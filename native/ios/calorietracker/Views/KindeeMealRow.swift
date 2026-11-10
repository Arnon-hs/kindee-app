import SwiftUI

struct KindeeMealRow: View {
    let meal: KindeeMeal
    let planned: Bool
    @Environment(\.dynamicTypeSize) private var typeSize
    var body: some View {
        VStack(alignment:.leading,spacing:8) {
            if planned { Text("\(meal.slot) · \(meal.modeLabel)").font(.subheadline.weight(.semibold)).foregroundStyle(.tint) }
            if typeSize.isAccessibilitySize {
                photo
                labels
            } else {
                HStack(alignment:.top,spacing:12) { photo; labels.frame(maxWidth:.infinity,alignment:.leading) }
            }
            if !meal.selectedOptions.isEmpty { Text("Selected: " + meal.selectedOptions.joined(separator:", ")).font(.footnote).foregroundStyle(.secondary) }
            if meal.needsSearch { Text("Recipe idea · no matching menu yet").font(.footnote).foregroundStyle(.secondary) }
            if meal.closed { Text("Closed at last check").font(.footnote).foregroundStyle(.secondary) }
            if meal.restaurant != nil {
                if let url = meal.link { Link(meal.providerName == "LINE MAN" ? "Open LINE MAN" : meal.providerName == "Grab" ? "Open Grab" : "View menu",destination:url).frame(minHeight:44) }
                DisclosureGroup("Menu details") {
                    VStack(alignment:.leading,spacing:8) {
                        Text("\(meal.providerName) · checked \(meal.checkedAt)")
                        if meal.isAI, let n = meal.nutrition {
                            Text("\(n["portion_description"] as? String ?? "One portion") · \(Int(KindeeMeal.number(n["portion_grams"]) ?? 0)) g assumed")
                            if let r = n["calorie_range"] as? [String:Any], let min = KindeeMeal.number(r["min"]), let max = KindeeMeal.number(r["max"]) {
                                Text("\(Int(min))–\(Int(max)) kcal")
                            }
                            if let total = n["total"] as? [String:Any] {
                                Text("P \(Int(KindeeMeal.number(total["protein_g"]) ?? 0)) g · C \(Int(KindeeMeal.number(total["carbs_g"]) ?? 0)) g · F \(Int(KindeeMeal.number(total["fat_g"]) ?? 0)) g")
                            }
                            Text("Estimated from menu text, not measured. Check ingredients and portion before logging.")
                        }
                        Text("Confirm ingredients, delivery address and final price. Menu photos may show another option.")
                    }.font(.footnote).foregroundStyle(.secondary).padding(.vertical,8)
                }.font(.subheadline)
            } else {
                Text("\(meal.raw["servings"] as? Int ?? 1) serving(s)").font(.footnote).foregroundStyle(.secondary)
            }
        }.padding(.vertical,8)
    }
    @ViewBuilder private var photo: some View {
        if let url = meal.photo {
            AsyncImage(url:url) { phase in
                switch phase {
                case .success(let image): image.resizable().scaledToFill()
                case .failure: Text("Photo unavailable").font(.caption).padding(8)
                default: ProgressView().accessibilityLabel("Loading menu photo")
                }
            }.frame(width:104,height:104).background(Color.secondary.opacity(0.08)).clipShape(RoundedRectangle(cornerRadius:14)).accessibilityHidden(true)
        }
    }
    private var labels: some View {
        VStack(alignment:.leading,spacing:4) {
            Text(meal.title).font(.headline).fixedSize(horizontal:false,vertical:true)
            if let restaurant = meal.restaurant { Text(restaurant).font(.subheadline) }
            if let price = meal.price { Text(price).font(.subheadline).foregroundStyle(.secondary) }
            Text(meal.calories.map { "≈ \(Int($0.rounded())) kcal · \(meal.isAI ? "AI estimate" : "recipe estimate")" } ?? "Calories not estimated").font(.subheadline).foregroundStyle(.secondary)
        }
    }
}
