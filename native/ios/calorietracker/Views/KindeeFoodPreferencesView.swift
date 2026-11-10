import SwiftUI

struct KindeeFoodPreferencesView: View {
    let initial: [String:Any]
    let onSave: ([String:Any],String) -> Void
    @State private var goal: String
    @State private var calories: Int
    @Environment(\.dismiss) private var dismiss
    @State private var diet: String
    @State private var meals: Int
    @State private var budget: Int
    @State private var minutes: Int
    @State private var choices: [String:Int]
    private let ingredients = ["chicken","shrimp","egg","tofu","rice","broccoli","tomato","spinach","garlic","olive"]
    init(initial:[String:Any],goal:String,target:Int,onSave:@escaping ([String:Any],String)->Void) {
        self.initial=initial;self.onSave=onSave
        _goal=State(initialValue:goal);_calories=State(initialValue:initial["daily_calories"] as? Int ?? target)
        _diet=State(initialValue:initial["diet"] as? String ?? "any")
        _meals=State(initialValue:initial["meals_per_day"] as? Int ?? 3)
        _budget=State(initialValue:initial["budget_per_meal"] as? Int ?? 350)
        _minutes=State(initialValue:initial["cooking_minutes"] as? Int ?? 30)
        var c:[String:Int]=[:]
        for i in initial["likes"] as? [String] ?? [] { c[i]=1 }
        for i in initial["excluded"] as? [String] ?? [] { c[i]=2 }
        _choices=State(initialValue:c)
    }
    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Picker("Goal",selection:$goal) {
                        Text("Track").tag("track");Text("Maintain").tag("maintain");Text("Lose weight").tag("lose");Text("Gain weight").tag("gain")
                    }
                    TextField("Daily calorie target",value:$calories,format:.number).keyboardType(.numberPad)
                    Text("Your chosen target. Menu calories are estimates.").font(.footnote).foregroundStyle(.secondary)
                    Picker("Food style",selection:$diet) {
                        Text("Any food").tag("any");Text("Vegetarian").tag("vegetarian");Text("Vegan").tag("vegan");Text("Pescatarian").tag("pescatarian")
                    }
                    Stepper("\(meals) meals per day",value:$meals,in:2...4)
                    Stepper("\(budget) THB per meal",value:$budget,in:1...2000,step:10)
                    Stepper("\(minutes) minutes to cook",value:$minutes,in:10...120,step:10)
                }
                Section("Ingredients") {
                    ForEach(ingredients,id:\.self) { ingredient in
                        Picker(ingredient.capitalized,selection:Binding(get:{choices[ingredient] ?? 0},set:{choices[ingredient]=$0})) {
                            Text("No preference").tag(0);Text("Like").tag(1);Text("Avoid").tag(2)
                        }
                    }
                }
                Button("Save food preferences") {
                    var food=initial
                    food["diet"]=diet;food["meals_per_day"]=meals;food["budget_per_meal"]=budget;food["cooking_minutes"]=minutes
                    food["likes"]=ingredients.filter{choices[$0]==1};food["excluded"]=ingredients.filter{choices[$0]==2}
                    food["daily_calories"]=calories
                    onSave(food,goal);dismiss()
                }.disabled(!(800...6000).contains(calories))
            }.navigationTitle("Make the plan yours")
                .toolbar { ToolbarItem(placement:.cancellationAction) { Button("Cancel"){dismiss()} } }
        }
    }
}
