import Foundation

struct KindeeMeal: Identifiable {
    let raw: [String: Any]
    var id: String { "\(raw["planned_for"] ?? "")-\(raw["meal_slot"] ?? "")-\(raw["id"] ?? raw["source_id"] ?? raw["title"] ?? "")" }
    var title: String { raw["title"] as? String ?? "Meal" }
    var date: String { raw["planned_for"] as? String ?? "" }
    var mode: String { raw["fulfillment"] as? String ?? "cook" }
    var modeLabel: String { mode == "cafe" ? "Cafe" : mode == "delivery" ? "Delivery" : "Cook" }
    var slot: String { (raw["meal_slot"] as? String ?? "").capitalized }
    var needsSearch: Bool { raw["offer_status"] as? String == "search_needed" }
    var menu: [String: Any] { raw["menu_item"] as? [String: Any] ?? raw }
    var nutrition: [String: Any]? { raw["nutrition"] as? [String: Any] ?? menu["nutrition"] as? [String: Any] }
    var isAI: Bool { nutrition?["source"] as? String == "ai_menu_estimate" }
    var calories: Double? { Self.number((nutrition?["total"] as? [String: Any])?["calories"]) }
    var restaurant: String? { menu["restaurant"] as? String }
    var closed: Bool { menu["restaurant_open"] as? Bool == false }
    var selectedOptions: [String] { menu["selected_options"] as? [String] ?? [] }
    var providerName: String {
        switch menu["provider"] as? String { case "merchant_site": return "Restaurant website"; case "grab": return "Grab"; case "line_man": return "LINE MAN"; default: return "Menu observation" }
    }
    var price: String? {
        guard let p = menu["price"] as? [String: Any], let amount = Self.number(p["amount_minor"]), amount >= 0,
              let currency = p["currency"] as? String else { return nil }
        return (menu["price_is_base"] as? Bool == true ? "From " : "") + "\((amount / 100).formatted(.number.precision(.fractionLength(0...2)))) \(currency)"
    }
    var photo: URL? {
        guard let s = menu["image_url"] as? String, let u = URLComponents(string:s), u.scheme == "https", u.host?.lowercased() == "img.wongnai.com",
              u.user == nil, u.password == nil, u.port == nil else { return nil }
        return u.url
    }
    var checkedAt: String {
        guard let value = menu["checked_at"] as? String else { return "Unknown" }
        var s = value.replacingOccurrences(of:" ",with:"T")
        if s.count == 19 { s += "Z" }
        let parser = ISO8601DateFormatter()
        if let d = parser.date(from:s) { return d.formatted(date:.abbreviated,time:.shortened) }
        parser.formatOptions.insert(.withFractionalSeconds)
        return parser.date(from:s)?.formatted(date:.abbreviated,time:.shortened) ?? value
    }
    var link: URL? {
        guard let s = menu["order_url"] as? String ?? menu["source_url"] as? String,
              var u = URLComponents(string:s), u.scheme == "https", u.user == nil, u.password == nil, u.port == nil,
              ["app.grab.com","food.grab.com","lin.ee","www.wongnai.com","vivarooftopandrestaurant.com"].contains(u.host?.lowercased() ?? "") else { return nil }
        // The supplier's af_dp value is an Android package scheme. Keep the verified HTTPS merchant route on iOS.
        if u.host?.lowercased() == "www.wongnai.com" { u.queryItems = u.queryItems?.filter { $0.name != "af_dp" } }
        return u.url
    }
    static func number(_ value: Any?) -> Double? {
        guard let n = value as? NSNumber else { return nil }
        let v = n.doubleValue
        return v.isFinite ? v : nil
    }
}

struct KindeeDaySummary {
    let target: Int
    let calories: Int
    let unknown: Int
    let hasAI: Bool
    init(meals: [KindeeMeal], target: Int) {
        self.target = target
        calories = Int(meals.compactMap(\.calories).reduce(0,+).rounded())
        unknown = meals.filter { $0.calories == nil }.count
        hasAI = meals.contains(where: \.isAI)
    }
}

/// Selects a suggestion by local date and time, without inferring what was eaten.
enum KindeeTodaySelection {
    static func meal(from meals: [KindeeMeal], for date: Date, now: Date, calendar: Calendar = .current) -> KindeeMeal? {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = calendar.timeZone
        formatter.dateFormat = "yyyy-MM-dd"
        let day = formatter.string(from: date)
        let slots = ["breakfast", "lunch", "dinner"]
        let matches = meals.filter { $0.date == day }.sorted {
            let a = slots.firstIndex(of: $0.slot.lowercased()) ?? slots.count
            let b = slots.firstIndex(of: $1.slot.lowercased()) ?? slots.count
            return a == b ? $0.id < $1.id : a < b
        }
        guard calendar.isDate(date, inSameDayAs: now) else { return matches.first }
        let hour = calendar.component(.hour, from: now)
        let slot = slots[hour < 11 ? 0 : hour < 16 ? 1 : 2]
        return matches.first { $0.slot.lowercased() == slot } ?? matches.first
    }
}
