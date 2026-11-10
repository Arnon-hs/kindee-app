import SwiftUI
import HealthKit

struct KindeePlanView: View {
    @StateObject private var server = KindeeServer()
    @StateObject private var location = KindeeLocation()
    @Environment(ProfileStore.self) private var profileStore
    @Environment(FoodStore.self) private var foodStore
    @Environment(HealthKitManager.self) private var health
    @State private var place: KindeePlace?
    @State private var profile: [String: Any] = [:]
    @State private var menus: [KindeeMeal] = []
    @State private var saved: [KindeeMeal] = []
    @State private var preview: [KindeeMeal] = []
    @State private var previewFrom = ""
    @State private var previewHash = ""
    @State private var requestID = UUID().uuidString
    @State private var allowUnknown = false
    @State private var selectedDay: String?
    @State private var planOptions = false
    @State private var nearby = false
    @State private var busy = false
    @State private var problem: String?
    @State private var notice: String?
    @State private var foodSheet = false
    @State private var accountSheet = false
    @State private var addressSheet = false
    @State private var query = ""
    @State private var matches: [KindeePlace] = []
    @State private var email = ""
    @State private var password = ""
    @State private var address = ""
    @State private var confirmTargets = false
    @State private var confirmSave = false
    @State private var confirmDiary = false
    @State private var confirmHealth = false
    @State private var workouts = false

    private func perform(_ action: @escaping @MainActor () async throws -> Void) {
        guard !busy else { return }
        Task { @MainActor in
            busy = true; problem = nil; notice = nil
            defer { busy = false }
            do { try await action() }
            catch { problem = error.localizedDescription }
            if !server.connected { profile = [:]; menus = []; saved = []; preview = []; place = nil }
        }
    }
    private func load() async throws {
        profile = try await server.profile()
        if let food = profile["food_preferences"] as? [String:Any] {
            allowUnknown = food["allow_unverified_menu"] as? Bool ?? false
            if let p = food["location"] as? [String:Any], let lat = p["latitude"] as? Double, let lng = p["longitude"] as? Double {
                place = KindeePlace(label:p["city"] as? String ?? "Selected address",latitude:lat,longitude:lng,source:p["source"] as? String ?? "manual")
            }
        }
        saved = try await server.saved()
        if let place { menus = try await server.menus(place) }
    }
    private func choose(_ next: KindeePlace) {
        guard !busy else { return }
        if let old=place,abs(old.latitude-next.latitude)>0.00001 || abs(old.longitude-next.longitude)>0.00001 { saved=[] }
        place = next; preview = []; addressSheet = false; matches = []
        perform { menus = try await server.menus(next) }
    }
    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("Your meal plan").font(.title.bold())
                }
                if busy { ProgressView("Loading Kindee…") }
                if let problem { Text(problem).foregroundStyle(.red) }
                if let notice { Text(notice).foregroundStyle(.secondary) }
                if !server.connected {
                    Section("Your meals, in one plan") {
                        Text("Connect Kindee for nearby menus and a mixed plan. Your local diary and AI scanner remain available in Diary.")
                        Button("Connect Kindee") { address = server.base; accountSheet = true }
                    }
                } else {
                    Section {
                        Button { addressSheet = true } label: { Label(place?.label ?? "Choose an address", systemImage:"mappin.and.ellipse") }
                        Text("5 km · " + server.menuStatus).font(.footnote).foregroundStyle(.secondary)
                        Picker("Show", selection:$nearby) { Text("My plan").tag(false); Text("Nearby menus").tag(true) }.pickerStyle(.segmented)
                    }
                    if !nearby {
                        planSections
                    } else {
                        Section("Nearby menus") {
                            if menus.isEmpty { Text("No menus here yet. Try another address or build a cooking plan.") }
                            ForEach(menus) { meal in KindeeMealRow(meal:meal,planned:false) }
                        }
                    }

                }
            }
            .disabled(busy)
            .scrollContentBackground(.hidden)
            .background(AppColors.appBackground)
            .navigationTitle("Kindee")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement:.topBarTrailing) { Button { address = server.base; accountSheet = true } label: { Image(systemName:"person.crop.circle") }.accessibilityLabel("Kindee account") } }
            .task { if server.connected { perform { try await load() } } }
            .onChange(of:location.place?.id) { _,_ in if let next = location.place { choose(next) } }
            .onChange(of:location.message) { _,s in problem = s }
            .sheet(isPresented:$workouts) { WorkoutsView() }
            .sheet(isPresented:$accountSheet) {
                NavigationStack {
                    Form {
                        if !server.connected {
                            TextField("Kindee server",text:$address).textInputAutocapitalization(.never).autocorrectionDisabled().keyboardType(.URL)
                            TextField("Email",text:$email).textContentType(.username).textInputAutocapitalization(.never).autocorrectionDisabled().keyboardType(.emailAddress)
                            SecureField("Password",text:$password).textContentType(.password)
                            Button("Sign in") { perform { try await server.login(address:address,email:email,password:password);password="";try await load();accountSheet=false } }.disabled(busy || email.isEmpty || password.isEmpty)
                        } else {
                            Text("Your plan belongs to this Kindee account. The food diary is local; sharing is a separate action.")
                            Text("Kindee AI sends submitted photos and food descriptions to OpenAI. Coach sends your message, profile and nutrition summary. Review estimates before logging.")
                            Button("Connect Kindee AI") { perform { try await server.connectFoodAI();notice="Kindee food and Coach AI connected";accountSheet=false } }
                    Section("Your diary and activity") {
                        Text("Sharing is optional. These uploads do not include food photos.").font(.footnote)
                        Button("Share today's reviewed diary") { confirmDiary = true }
                        Button("Connect Apple Health") {
                            perform {
                                try await health.requestKindeeActivityAuthorization()
                                notice = "Health permissions requested. Choose the data to share in Apple Health."
                            }
                        }
                        Button("Share today's activity") { confirmHealth = true }
                        Button("Workout diary") { workouts = true }
                    }
                            Button("Use Kindee nutrition targets") { confirmTargets = true }
                            Button("Sign out of Kindee",role:.destructive) { perform { await server.logout();accountSheet=false } }
                        }
                        if let problem { Text(problem).foregroundStyle(.red) }
                    }.navigationTitle("Kindee account").toolbar { ToolbarItem(placement:.cancellationAction) { Button("Done") { accountSheet=false }.disabled(busy) } }
                }.interactiveDismissDisabled(busy)
            }
            .sheet(isPresented:$foodSheet) {
                KindeeFoodPreferencesView(initial:profile["food_preferences"] as? [String:Any] ?? [:],goal:profile["goal_kind"] as? String ?? "track",target:targetCalories) { food,goal in
                    perform {
                        guard let place else { throw KindeeServerError.message("Choose an address first") }
                        try await server.updateLocation(profile:profile,place:place,allowUnknown:allowUnknown,preferences:food,goal:goal)
                        preview=[];try await load();notice="Food preferences saved"
                    }
                }
            }
            .sheet(isPresented:$addressSheet) {
                NavigationStack {
                    Form {
                        TextField("Address or place",text:$query).onChange(of:query) { _,_ in matches=[] }
                        Text("Search sends this address to OpenStreetMap Nominatim.").font(.footnote)
                        Button("Search") { perform { matches=try await server.search(query); if matches.isEmpty { problem="No matching address. Try a more specific place." } } }.disabled(busy || query.count<3)
                        ForEach(matches) { p in Button(p.label) { choose(p) } }
                        Button("Use current location") { location.locate() }.disabled(busy)
                        Button("Homa Phuket Town · test location") { choose(.homa) }.disabled(busy)
                        if let problem { Text(problem).foregroundStyle(.red) }
                    }.navigationTitle("Where will you eat?").toolbar { ToolbarItem(placement:.cancellationAction) { Button("Done") { addressSheet=false }.disabled(busy) } }
                }.interactiveDismissDisabled(busy)
            }
            .confirmationDialog("Replace diary targets with Kindee calorie and macro targets? Automatic adjustments stay off.",isPresented:$confirmTargets,titleVisibility:.visible) {
                Button("Use targets") { perform {
                    let data = try await server.request("dashboard")["data"] as? [String:Any] ?? [:]
                    guard let g = (data["nutrition"] as? [String:Any])?["goals"] as? [String:Any],let kcal=g["calories"] as? Int,(800...6000).contains(kcal),let protein=g["protein_g"] as? Int,let fat=g["fat_g"] as? Int,let carbs=g["carbs_g"] as? Int else { throw KindeeServerError.message("Review your Kindee targets first") }
                    var p=profileStore.profile;p.customCalories=kcal;p.customProtein=protein;p.customFat=fat;p.customCarbs=carbs;p.autoBalanceMacro=nil;p.save()
                    UserDefaults.standard.set(false,forKey:"adaptiveGoalsEnabled");notice="Kindee targets applied";accountSheet=false
                } }
            }
            .confirmationDialog("Save this week? Generated meals in the selected seven days are replaced. Manually added meals stay. No food is logged as eaten.",isPresented:$confirmSave,titleVisibility:.visible) {
                Button("Save week") { perform {
                    _ = try await server.request("meal-plans/personalized",method:"POST",body:["from":previewFrom,"preview_hash":previewHash,"request_id":requestID,"replace_generated":true])
                    saved = try await server.saved(); preview = []; notice = "Your meal plan is saved"
                } }
            }
            .confirmationDialog("Share today's reviewed diary with Kindee? Meal names, time and nutrition are sent. Photos are not included. Repeated uploads update the same entries; server deletions are separate.",isPresented:$confirmDiary,titleVisibility:.visible) {
                Button("Share diary") { perform {
                    let today = foodStore.entries.filter { Calendar.current.isDateInToday($0.timestamp) }
                    let count = try await server.shareDiary(today); notice="Shared \(count) reviewed entries"
                } }
            }
            .confirmationDialog("Share today's available steps and energy with Kindee? This aggregate becomes the selected activity source. Your calorie target will not be increased automatically.",isPresented:$confirmHealth,titleVisibility:.visible) {
                Button("Share activity") { perform {
                    let zone = TimeZone.current.identifier
                    let dashboard = try await server.request("dashboard")
                    guard (dashboard["data"] as? [String:Any])?["timezone"] as? String == zone else { throw KindeeServerError.message("Phone and account timezones must match") }
                    async let e = health.readEnergyForDay(Date())
                    async let s = health.fetchStepsForDay(Date())
                    let (energy,steps) = await (e,s)
                    guard energy != nil || steps != nil else { throw KindeeServerError.message("No activity data found. Sync your watch in its official app first.") }
                    let f=DateFormatter();f.dateFormat="yyyy-MM-dd";f.locale=Locale(identifier:"en_US_POSIX")
                    _ = try await server.request("health/sync",method:"POST",body:["provider":"healthkit","origin":"healthkit_aggregate","date":f.string(from:Date()),"timezone":zone,"active_calories_kcal":energy?.activeCalories as Any? ?? NSNull(),"total_energy_kcal":energy?.totalCalories as Any? ?? NSNull(),"steps":steps as Any? ?? NSNull(),"collected_at":ISO8601DateFormatter().string(from:Date()),"consent":true,"select_source":true])
                    notice="Activity shared with Kindee"
                } }
            }
        }
    }
    private var targetCalories: Int {
        let food = profile["food_preferences"] as? [String:Any]
        return (food?["daily_calories"] as? Int) ?? (profile["calorie_goal"] as? Int) ?? 2000
    }
    private var rows: [KindeeMeal] { preview.isEmpty ? saved : preview }
    private var days: [String] { Array(Set(rows.map(\.date).filter{!$0.isEmpty})).sorted() }
    private var activeDay: String? {
        if let selectedDay, days.contains(selectedDay) { return selectedDay }
        let today = Self.dateKey(Date())
        return days.contains(today) ? today : days.first
    }
    private static func dateKey(_ date: Date) -> String {
        let f=DateFormatter();f.locale=Locale(identifier:"en_US_POSIX");f.dateFormat="yyyy-MM-dd";return f.string(from:date)
    }
    private func dayLabel(_ day:String) -> String {
        if day == Self.dateKey(Date()) { return "Today" }
        let f=DateFormatter();f.locale=Locale(identifier:"en_US_POSIX");f.dateFormat="yyyy-MM-dd"
        guard let date=f.date(from:day) else { return day }
        return date.formatted(.dateTime.weekday(.abbreviated).day())
    }
    @ViewBuilder private var planSections: some View {
        if !days.isEmpty {
            Section {
                ScrollView(.horizontal,showsIndicators:false) {
                    HStack(spacing:8) {
                        ForEach(days,id:\.self) { day in
                            Button(dayLabel(day)) { selectedDay=day }.buttonStyle(.bordered)
                                .tint(day == activeDay ? Color.accentColor : Color.secondary)
                                .accessibilityAddTraits(day == activeDay ? .isSelected : [])
                        }
                    }
                }
                if !preview.isEmpty { Text("Preview · not saved").font(.subheadline).foregroundStyle(.secondary) }
            }
        }
        Section {
            HStack {
                Button("Preferences") { foodSheet=true }
                Spacer()
                Button(planOptions ? "Close options" : "Plan options") { planOptions.toggle() }
            }.buttonStyle(.borderless)
            if planOptions {
                Toggle("Use AI menu estimates",isOn:$allowUnknown).onChange(of:allowUnknown) { _,_ in preview=[] }
                Text("Menu names and selected options are sent to OpenAI. Estimates are saved for seven days; review portions.").font(.footnote).foregroundStyle(.secondary)
            }
            Button(saved.isEmpty ? "Build my week" : "Rebuild week") { perform { try await build() } }.disabled(place == nil)
        }
        if !preview.isEmpty {
            Section {
                Text("Saving a plan does not log food as eaten.").font(.footnote)
                Button("Save this plan") { confirmSave=true }
                Button("Discard preview",role:.cancel) { preview=[] }
            }
        }
        let meals = rows.filter { $0.date == activeDay }
        if meals.isEmpty {
            Section { Text("No saved plan here. Choose an address and build your week.") }
        } else {
            Section {
                let summary = KindeeDaySummary(meals:meals,target:targetCalories)
                let goal = ["track":"Track","maintain":"Maintain","lose":"Lose weight","gain":"Gain weight"][profile["goal_kind"] as? String ?? "track"] ?? "Track"
                Text("\(goal) · \(summary.target) kcal goal").font(.subheadline).foregroundStyle(.secondary)
                Text(summary.unknown == 0 ? "≈ \(summary.calories) kcal planned" : "\(summary.calories) kcal + \(summary.unknown) unestimated").font(.headline)
                if summary.hasAI { Text("Includes AI estimates · check portions").font(.footnote).foregroundStyle(.secondary) }
                if restricted { Text("Ingredient restrictions on. Unverified restaurant meals stay out of your plan.").font(.footnote).foregroundStyle(.secondary) }
            }
            Section { ForEach(meals) { meal in KindeeMealRow(meal:meal,planned:true) } }
        }
    }
    private var restricted: Bool {
        let food=profile["food_preferences"] as? [String:Any] ?? [:]
        return (food["diet"] as? String ?? "any") != "any" || !(food["excluded"] as? [String] ?? []).isEmpty
    }
    private func build() async throws {
        guard let place else { throw KindeeServerError.message("Choose an address first") }
        try await server.updateLocation(profile:profile,place:place,allowUnknown:allowUnknown)
        menus = try await server.menus(place)
        if allowUnknown && !restricted { try await server.estimateMenus();menus = try await server.menus(place) }
        let r=try await server.request("meal-plans/personalized/preview",method:"POST",body:["from":Self.dateKey(Date())])
        guard let d=r["data"] as? [String:Any],let from=d["from"] as? String,let hash=d["preview_hash"] as? String,let entries=d["entries"] as? [[String:Any]] else { throw KindeeServerError.message("Invalid meal plan response") }
        previewFrom=from;previewHash=hash;requestID=UUID().uuidString;preview=entries.map{KindeeMeal(raw:$0)}
    }
}
