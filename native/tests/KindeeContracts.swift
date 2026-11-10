import Foundation

@main struct KindeeContractChecks {
    static func main() throws {
        let data=Data("""
        {"title":"Rice","fulfillment":"delivery","nutrition":{"source":"ai_menu_estimate","total":{"calories":680},"calorie_range":{"min":500,"max":920}},"menu_item":{"provider":"line_man","restaurant":"Tati","price":{"amount_minor":7900,"currency":"THB"},"selected_options":["Chicken"],"image_url":"https://img.wongnai.com/p/256x256/dish.jpg","order_url":"https://www.wongnai.com/delivery/businesses/3361848Uq/order?af_dp=com.linecorp.linemanth%3A%2F%2Fapp","restaurant_open":false}}
        """.utf8)
        let meal=KindeeMeal(raw:try JSONSerialization.jsonObject(with:data) as! [String:Any])
        precondition(meal.calories==680 && meal.isAI && meal.closed)
        precondition(meal.price?.contains("79")==true && meal.photo?.host=="img.wongnai.com")
        precondition(meal.link?.query?.contains("af_dp") != true)
        precondition(meal.selectedOptions==["Chicken"])
        var bad=meal.menu;bad["image_url"]="https://private.example/dish.jpg";precondition(KindeeMeal(raw:bad).photo==nil)
        bad["order_url"]="https://user:pass@www.wongnai.com/order";precondition(KindeeMeal(raw:bad).link==nil)
        bad["image_url"]="http://img.wongnai.com/dish.jpg";precondition(KindeeMeal(raw:bad).photo==nil)
        let unknown=KindeeMeal(raw:["title":"Unknown","nutrition":NSNull()])
        let summary=KindeeDaySummary(meals:[meal,unknown],target:2000)
        precondition(summary.calories==680 && summary.unknown==1 && summary.hasAI)
        let base="https://kindee-api.158.69.190.195.sslip.io/api/v1"
        precondition(KindeeCoachPayload.isServerEndpoint(base+"/ai",server:base))
        precondition(!KindeeCoachPayload.isServerEndpoint("https://evil.test/api/v1/ai",server:base))
        precondition(!KindeeCoachPayload.isServerEndpoint(base+"/ai?key=bad",server:base))
        let messages=KindeeCoachPayload.messages(system:String(repeating:"字",count:20000),history:Array(repeating:("user",String(repeating:"字",count:10000)),count:50),question:String(repeating:"字",count:10000))
        precondition(messages.count==8)
        precondition(messages.reduce(0){$0+($1["content"] as! String).utf8.count}<24000)
        precondition(messages.allSatisfy{Set($0.keys)==Set(["role","content"])})
        let all=KindeeDaySummary(meals:[meal],target:2200);precondition(all.target==2200 && all.unknown==0)
        var bangkok = Calendar(identifier: .gregorian)
        bangkok.timeZone = TimeZone(identifier: "Asia/Bangkok")!
        let noon = ISO8601DateFormatter().date(from: "2026-11-10T05:00:00Z")!
        let sample = ["breakfast", "lunch", "dinner"].map { KindeeMeal(raw: ["id": $0, "title": $0, "meal_slot": $0, "planned_for": "2026-11-10"]) }
        assert(KindeeTodaySelection.meal(from: sample, for: noon, now: noon, calendar: bangkok)?.title == "lunch")
        let morning = noon.addingTimeInterval(-6 * 3600)
        assert(KindeeTodaySelection.meal(from: sample, for: morning, now: morning, calendar: bangkok)?.title == "breakfast")
        let evening = noon.addingTimeInterval(6 * 3600)
        assert(KindeeTodaySelection.meal(from: sample, for: evening, now: evening, calendar: bangkok)?.title == "dinner")
        assert(KindeeTodaySelection.meal(from: sample, for: noon.addingTimeInterval(86400), now: noon, calendar: bangkok) == nil)
        var buddhist = Calendar(identifier: .buddhist)
        buddhist.timeZone = bangkok.timeZone
        guard KindeeTodaySelection.meal(from: sample, for: noon, now: noon, calendar: buddhist)?.title == "lunch" else {
            print("FAIL: Bangkok Buddhist calendar must match Gregorian API date")
            exit(1)
        }
        print("PASS: local date/slot plan selection;  meal decoding, prices, image allowlist, links, AI labels, day totals, coach origin and byte limits")
    }
}
