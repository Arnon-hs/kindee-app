import SwiftUI

struct KindeeTodayPlanView: View {
    let date: Date
    let onOpenPlan: () -> Void
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var meal: KindeeMeal?
    @State private var loading = true
    @State private var message: String?
    @State private var loadGeneration = 0

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            let headerLayout = dynamicTypeSize.isAccessibilitySize
                ? AnyLayout(VStackLayout(alignment: .leading, spacing: 4))
                : AnyLayout(HStackLayout())
            headerLayout {
                Text("From your plan", tableName: "Kindee").font(.title3.bold())
                if !dynamicTypeSize.isAccessibilitySize { Spacer() }
                Button(action: onOpenPlan) { Text("View plan", tableName: "Kindee") }.frame(minHeight: 44)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            if loading {
                ProgressView { Text("Loading saved plan…", tableName: "Kindee") }
            } else if let meal {
                KindeeMealRow(meal: meal, planned: true)
                Text("Planned, not logged as eaten.", tableName: "Kindee").font(.footnote).foregroundStyle(Color.primary.opacity(0.72))
            } else {
                Text(message ?? String(localized: "Your next meal starts with a plan.", table: "Kindee")).foregroundStyle(Color.primary.opacity(0.72))
                Button(action: onOpenPlan) { Text("Open meal plan", tableName: "Kindee") }.frame(minHeight: 44)
            }
        }
        .padding(.vertical, 8)
        .task(id: date) { await load() }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { Task { await load() } }
        }
    }

    @MainActor private func load() async {
        loadGeneration += 1
        let generation = loadGeneration
        loading = true
        meal = nil
        message = nil
        defer { if generation == loadGeneration { loading = false } }
        let server = KindeeServer()
        guard server.connected else {
            message = String(localized: "Connect Kindee to see your saved meals here. Your diary works independently.", table: "Kindee")
            return
        }
        do {
            let saved = try await server.saved()
            guard !Task.isCancelled, generation == loadGeneration else { return }
            meal = KindeeTodaySelection.meal(from: saved, for: date, now: Date())
            if meal == nil { message = String(localized: "No saved meals for this day. Build a mixed plan with cooking, cafes and delivery.", table: "Kindee") }
        } catch {
            if !Task.isCancelled, generation == loadGeneration { message = String(localized: "Couldn't load your saved plan. Open Plan to try again.", table: "Kindee") }
        }
    }
}
