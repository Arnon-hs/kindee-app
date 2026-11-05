import Capacitor
import HealthKit

@objc(KinDeeHealthPlugin)
public final class KinDeeHealthPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "KinDeeHealthPlugin"
    public let jsName = "KinDeeHealth"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "availability", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "authorize", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "sources", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "readDaily", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "clearSession", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "disconnect", returnType: CAPPluginReturnPromise)
    ]
    private let store = HKHealthStore()
    private let energy = HKQuantityType(.activeEnergyBurned)
    private let steps = HKQuantityType(.stepCount)

    @objc func availability(_ call: CAPPluginCall) {
        call.resolve(["status": HKHealthStore.isHealthDataAvailable() ? "available" : "unavailable", "provider": "healthkit"])
    }

    @objc func authorize(_ call: CAPPluginCall) {
        guard HKHealthStore.isHealthDataAvailable() else { call.reject("health_unavailable"); return }
        store.requestAuthorization(toShare: [], read: [energy, steps]) { completed, error in
            guard completed, error == nil else { call.reject("health_authorization_failed"); return }
            // HealthKit deliberately hides read authorization; completion does not imply access.
            call.resolve(["status": "requested"])
        }
    }

    private func interval(_ call: CAPPluginCall) -> DateInterval? {
        guard let zoneName = call.getString("timezone"), let zone = TimeZone(identifier: zoneName),
              let value = call.getString("date") else { return nil }
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = zone
        formatter.dateFormat = "yyyy-MM-dd"
        guard let start = formatter.date(from: value) else { return nil }
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = zone
        guard let next = calendar.date(byAdding: .day, value: 1, to: start) else { return nil }
        let end = min(next, Date())
        return start < end ? DateInterval(start: start, end: end) : nil
    }

    private func findSources(_ interval: DateInterval, completion: @escaping (Set<HKSource>, Error?) -> Void) {
        let group = DispatchGroup()
        let lock = NSLock()
        var sources = Set<HKSource>()
        var failure: Error?
        let predicate = HKQuery.predicateForSamples(withStart: interval.start, end: interval.end, options: [])
        for type in [energy, steps] {
            group.enter()
            store.execute(HKSourceQuery(sampleType: type, samplePredicate: predicate) { _, result, error in
                lock.lock()
                sources.formUnion(result ?? [])
                if let error { failure = error }
                lock.unlock()
                group.leave()
            })
        }
        group.notify(queue: .main) { completion(sources, failure) }
    }

    @objc func sources(_ call: CAPPluginCall) {
        guard let interval = interval(call) else { call.reject("invalid_day"); return }
        findSources(interval) { sources, error in
            if error != nil { call.reject("health_read_failed"); return }
            call.resolve(["origins": Array(Set(sources.map(\.bundleIdentifier))).sorted()])
        }
    }

    @objc func readDaily(_ call: CAPPluginCall) {
        guard let interval = interval(call), let origin = call.getString("origin") else { call.reject("invalid_day_or_source"); return }
        findSources(interval) { sources, error in
            let selected = Set(sources.filter { $0.bundleIdentifier == origin })
            guard error == nil, !selected.isEmpty else { call.reject("health_no_data"); return }
            let time = HKQuery.predicateForSamples(withStart: interval.start, end: interval.end, options: [])
            let source = HKQuery.predicateForObjects(from: selected)
            let predicate = NSCompoundPredicate(andPredicateWithSubpredicates: [time, source])
            let group = DispatchGroup()
            let lock = NSLock()
            var values: [String: Any] = ["provider": "healthkit", "origin": origin,
                "active_calories_kcal": NSNull(), "steps": NSNull(), "total_energy_kcal": NSNull()]
            var failed = false
            for (type, key, unit) in [(self.energy, "active_calories_kcal", HKUnit.kilocalorie()), (self.steps, "steps", HKUnit.count())] {
                group.enter()
                self.store.execute(HKStatisticsQuery(quantityType: type, quantitySamplePredicate: predicate, options: .cumulativeSum) { _, result, error in
                    lock.lock()
                    if error != nil { failed = true }
                    if let quantity = result?.sumQuantity() {
                        let value = quantity.doubleValue(for: unit)
                        if value.isFinite { values[key] = key == "steps" ? Double(Int(value)) : value }
                    }
                    lock.unlock()
                    group.leave()
                })
            }
            group.notify(queue: .main) {
                if failed { call.reject("health_read_failed"); return }
                values["collected_at"] = ISO8601DateFormatter().string(from: Date())
                call.resolve(values)
            }
        }
    }

    @objc func clearSession(_ call: CAPPluginCall) { call.resolve() }

    @objc func disconnect(_ call: CAPPluginCall) {
        // Apps cannot revoke HealthKit permission themselves; the user uses Settings > Health.
        call.resolve(["requiresSystemSettings": true])
    }
}
