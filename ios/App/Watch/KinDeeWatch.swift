import SwiftUI
import HealthKit

@main
struct KinDeeWatchApp: App {
    var body: some Scene { WindowGroup { ActivityView() } }
}

@MainActor
final class ActivityModel: ObservableObject {
    @Published var energy: Double?
    @Published var steps: Double?
    @Published var message = "Allow Health access, then refresh. Sync your nutrition account in KinDee on iPhone."
    private let store = HKHealthStore()
    private let active = HKQuantityType(.activeEnergyBurned)
    private let step = HKQuantityType(.stepCount)
    func refresh() {
        guard HKHealthStore.isHealthDataAvailable() else { message = "Health is unavailable."; return }
        store.requestAuthorization(toShare: [], read: [active, step]) { completed, error in
            guard completed, error == nil else { Task { @MainActor in self.message = "Unable to request Health access." }; return }
            // Daily HealthKit totals are for display; the iPhone imports one explicit source.
            for (type, unit, isEnergy) in [(self.active, HKUnit.kilocalorie(), true), (self.step, HKUnit.count(), false)] {
                let start = Calendar.current.startOfDay(for: Date())
                let predicate = HKQuery.predicateForSamples(withStart: start, end: Date(), options: [])
                self.store.execute(HKStatisticsQuery(quantityType: type, quantitySamplePredicate: predicate, options: .cumulativeSum) { _, result, _ in
                    let value = result?.sumQuantity()?.doubleValue(for: unit)
                    Task { @MainActor in
                        if isEnergy { self.energy = value } else { self.steps = value }
                        self.message = "No reading can mean missing data or permission. Sync selected sources on iPhone."
                    }
                })
            }
        }
    }
}

struct ActivityView: View {
    @StateObject private var model = ActivityModel()
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("KinDee").font(.title2).foregroundStyle(.green)
                Text("Active energy").font(.caption)
                Text(model.energy.map { "\(Int($0)) kcal" } ?? "No data").font(.title3)
                Text(model.steps.map { "\(Int($0)) steps" } ?? "No steps data")
                Button("Refresh", action: model.refresh)
                Text(model.message).font(.footnote)
            }
        }
    }
}
