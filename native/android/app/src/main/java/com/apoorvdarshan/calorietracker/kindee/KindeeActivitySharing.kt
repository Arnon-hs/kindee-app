package com.apoorvdarshan.calorietracker.kindee

import com.apoorvdarshan.calorietracker.services.health.HealthConnectManager
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

internal suspend fun shareKindeeActivity(client: KindeeClient, health: HealthConnectManager) {
    val zone = ZoneId.systemDefault()
    val accountZone = client.request("dashboard").getJSONObject("data").getString("timezone")
    require(accountZone == zone.id) { "Phone and Kindee account timezones must match before sharing a daily total." }
    val date = LocalDate.now(zone)
    val energy = health.readEnergyForDay(date)
    val steps = health.readStepsForDay(date)
    require(energy != null || steps != null) { "No activity data found. Sync your watch in its official app, then try again." }
    client.request("health/sync", "POST", JSONObject()
        .put("provider","health_connect").put("origin","health_connect_aggregate")
        .put("date",date.toString()).put("timezone",zone.id)
        .put("active_calories_kcal", energy?.activeCalories ?: JSONObject.NULL)
        .put("total_energy_kcal", energy?.totalCalories ?: JSONObject.NULL)
        .put("steps", steps ?: JSONObject.NULL)
        .put("collected_at",Instant.now().toString()).put("consent",true).put("select_source",true))
}
