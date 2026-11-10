import Foundation
import Combine
import Security

struct KindeePlace: Identifiable {
    var id: String { "\(latitude),\(longitude)" }
    let label: String
    let latitude: Double
    let longitude: Double
    var source = "address"
    static let homa = KindeePlace(label: "Homa Phuket Town, 3/41 Soi Samkong 1", latitude: 7.9080136, longitude: 98.3785166, source: "manual")
}

private final class KindeeNoRedirect: NSObject, URLSessionTaskDelegate, @unchecked Sendable {
    func urlSession(_ session: URLSession, task: URLSessionTask, willPerformHTTPRedirection response: HTTPURLResponse, newRequest request: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) { completionHandler(nil) }
}

enum KindeeServerError: LocalizedError {
    case message(String)
    var errorDescription: String? { if case .message(let s) = self { return s }; return nil }
}

@MainActor
final class KindeeServer: ObservableObject {
    @Published private(set) var connected = false
    @Published private(set) var menuStatus = "Saved menus · Check availability"
    @Published var base: String
    private let session = URLSession(configuration: .ephemeral, delegate: KindeeNoRedirect(), delegateQueue: nil)
    init() {
        base = UserDefaults.standard.string(forKey: "kindee.server") ?? "https://kindee-api.158.69.190.195.sslip.io/api/v1"
        connected = KeychainHelper.load(key: "kindee.token." + base) != nil
    }
    static func validatedBase(_ value: String) throws -> String {
        let s = value.trimmingCharacters(in: .whitespacesAndNewlines).trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        guard let u = URLComponents(string: s), u.user == nil, u.password == nil, u.query == nil, u.fragment == nil, u.path == "/api/v1", u.host != nil else { throw KindeeServerError.message("Enter a server address ending in /api/v1") }
        #if DEBUG
        let local = u.scheme == "http" && u.host == "127.0.0.1"
        #else
        let local = false
        #endif
        guard u.scheme == "https" || local else { throw KindeeServerError.message("Use HTTPS for your server") }
        return s
    }
    func request(_ path: String, method: String = "GET", body: [String: Any]? = nil, auth: Bool = true, address: String? = nil) async throws -> [String: Any] {
        let origin = try Self.validatedBase(address ?? base)
        guard !path.hasPrefix("/"), !path.contains("://"), !path.contains(".."), let url = URL(string: origin + "/" + path) else { throw KindeeServerError.message("Invalid API path") }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.timeoutInterval = (path == "meal-plans/menu-nutrition" || path == "ai/chat/completions") ? 75 : 40
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if auth {
            guard let token = KeychainHelper.load(key: "kindee.token." + origin) else { throw KindeeServerError.message("Sign in to Kindee") }
            request.setValue("Bearer " + token, forHTTPHeaderField: "Authorization")
        }
        if let body {
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        let (data, response) = try await session.data(for: request)
        guard let response = response as? HTTPURLResponse else { throw KindeeServerError.message("No server response") }
        if response.statusCode == 401 {
            if auth { KeychainHelper.delete(key: "kindee.token." + origin); connected = false }
            throw KindeeServerError.message(auth ? "Session expired. Sign in again." : "Check email and password.")
        }
        guard (200..<300).contains(response.statusCode) else {
            let message: String
            switch response.statusCode {
            case 409: message = "Menu changed. Preview your plan again."
            case 402: message = "Kindee AI is unavailable or its request limit was reached. Turn off menu estimates to build a cooking plan."
            case 422: message = "Check your address and preferences. Restaurant estimates require an unrestricted diet."
            case 429: message = "Please wait before trying again."
            default: message = "Kindee request failed (\(response.statusCode)). Try again."
            }
            throw KindeeServerError.message(message)
        }
        if response.statusCode == 204 { return [:] }
        guard data.count <= 4 * 1024 * 1024, let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else { throw KindeeServerError.message("Invalid server response") }
        return json
    }
    func login(address: String, email: String, password: String) async throws {
        let next = try Self.validatedBase(address)
        let r = try await request("auth/login", method: "POST", body: ["email":email.trimmingCharacters(in: .whitespacesAndNewlines), "password":password], auth: false, address: next)
        guard let data = r["data"] as? [String: Any], let token = data["token"] as? String, (20...1024).contains(token.count) else { throw KindeeServerError.message("Invalid session response") }
        guard KeychainHelper.save(key: "kindee.token." + next, value: token) == errSecSuccess,
              KeychainHelper.load(key: "kindee.token." + next) == token else {
            throw KindeeServerError.message("Sign-in could not be saved securely. Please try again.")
        }
        base = next
        UserDefaults.standard.set(next, forKey: "kindee.server")
        connected = true
    }
    func connectFoodAI() async throws {
        let status = try await request("ai/status")["data"] as? [String:Any] ?? [:]
        guard status["configured"] as? Bool == true, (status["remaining"] as? Int ?? 0) > 0,
              let model = status["model"] as? String, let token = KeychainHelper.load(key:"kindee.token." + base) else { throw KindeeServerError.message("Kindee AI is unavailable or its request limit has been reached.") }
        AIModeSettings.mode = .byok
        AIProviderSettings.setAPIKey(token,for:.customOpenAI)
        AIProviderSettings.setCustomBaseURL(base + "/ai",for:.customOpenAI)
        AIProviderSettings.selectedModel = model
        AIProviderSettings.selectedProvider = .customOpenAI
        AIProviderSettings.separateTextProviderEnabled = false
        AIProviderSettings.fallbackEnabled = false
    }
    func logout() async {
        _ = try? await request("auth/logout", method: "POST", body: [:])
        if AIProviderSettings.apiKey(for:.customOpenAI) == KeychainHelper.load(key:"kindee.token." + base) { AIProviderSettings.setAPIKey(nil,for:.customOpenAI) }
        KeychainHelper.delete(key: "kindee.token." + base)
        connected = false
    }
    func profile() async throws -> [String: Any] { try await request("onboarding")["data"] as? [String: Any] ?? [:] }
    func menus(_ p: KindeePlace) async throws -> [KindeeMeal] {
        var q = URLComponents()
        q.queryItems = [URLQueryItem(name:"lat",value:String(p.latitude)),URLQueryItem(name:"lng",value:String(p.longitude)),URLQueryItem(name:"radius_m",value:"5000"),URLQueryItem(name:"q",value:String(p.label.prefix(200)))]
        let r = try await request("meal-plans/nearby-menu?" + (q.percentEncodedQuery ?? ""))
        let meta = r["meta"] as? [String:Any]
        let state = ((meta?["providers"] as? [String:Any])?["line_man"] as? [String:Any])?["status"] as? String
        switch state {
        case "updated", "partial": menuStatus = "LINE MAN · Auto refresh · Check delivery"
        case "unavailable": menuStatus = "Refresh unavailable · Saved menus"
        case "no_coverage": menuStatus = "No new menus here"
        case "rate_limited", "refreshing": menuStatus = "Menu update pending"
        default: menuStatus = "Saved menus · Check availability"
        }
        return (r["data"] as? [[String: Any]] ?? []).map { KindeeMeal(raw: $0) }
    }
    func saved() async throws -> [KindeeMeal] {
        let r = try await request("meal-plans")
        return ((r["data"] as? [String: Any])?["entries"] as? [[String: Any]] ?? []).map { KindeeMeal(raw: $0) }
    }
    func search(_ query: String) async throws -> [KindeePlace] {
        let r = try await request("meal-plans/location/search", method: "POST", body: ["query":query])
        return (r["data"] as? [[String: Any]] ?? []).compactMap {
            guard let label = $0["label"] as? String, let lat = $0["latitude"] as? Double, let lng = $0["longitude"] as? Double else { return nil }
            return KindeePlace(label:label,latitude:lat,longitude:lng)
        }
    }
    func updateLocation(profile: [String: Any], place: KindeePlace, allowUnknown: Bool, preferences: [String:Any]? = nil, goal: String? = nil) async throws {
        var body = profile.filter { ["goal_kind","height_cm","baseline_kg","target_kg","activity_level","health_preference"].contains($0.key) }
        var food = preferences ?? profile["food_preferences"] as? [String: Any] ?? ["diet":"any","likes":[],"excluded":[],"meals_per_day":3,"cooking_minutes":30,"budget_per_meal":350]
        food["location"] = ["city":place.label,"latitude":place.latitude,"longitude":place.longitude,"radius_km":5,"source":place.source]
        food["meal_mode"] = "mixed"
        food["allow_unverified_menu"] = allowUnknown
        if let goal { body["goal_kind"] = goal }
        body["food_preferences"] = food
        body["consent"] = true
        _ = try await request("onboarding",method:"PUT",body:body)
    }
    func estimateMenus() async throws {
        _ = try await request("meal-plans/menu-nutrition",method:"POST",body:["consent":true])
    }
    func shareDiary(_ entries: [FoodEntry]) async throws -> Int {
        guard !entries.isEmpty, entries.count <= 100 else { throw KindeeServerError.message("Select 1 to 100 reviewed entries") }
        let rows: [[String: Any]] = entries.map { e in
            ["client_entry_id":e.id.uuidString.lowercased(),"title":e.name,"source":e.source == .manual ? "manual" : e.source == .barcode ? "barcode" : "ai_estimate",
             "eaten_at":ISO8601DateFormatter().string(from:e.timestamp),"calories":e.calories,"protein_g":e.protein,"fat_g":e.fat,"carbs_g":e.carbs,"user_reviewed":true]
        }
        let r = try await request("food-entries/native",method:"POST",body:["consent":true,"entries":rows])
        return (r["data"] as? [String:Any])?["count"] as? Int ?? 0
    }

}
