package com.apoorvdarshan.calorietracker.kindee

import com.apoorvdarshan.calorietracker.models.FoodEntry
import com.apoorvdarshan.calorietracker.models.FoodSource
import org.json.JSONArray
import org.json.JSONObject

internal suspend fun shareKindeeDiary(client: KindeeClient, entries: List<FoodEntry>): Int {
    val own = entries.filter { it.healthConnectOrigin == null }
    require(own.isNotEmpty()) { "No reviewed entries to share today." }
    require(own.size <= 100) { "Share up to 100 entries at a time." }
    val body = JSONArray()
    for (entry in own) body.put(JSONObject()
        .put("client_entry_id",entry.id.toString()).put("title",entry.name)
        .put("source",when(entry.source) {FoodSource.MANUAL -> "manual"; FoodSource.BARCODE -> "barcode"; else -> "ai_estimate"})
        .put("eaten_at",entry.timestamp.toString()).put("calories",entry.calories)
        .put("protein_g",entry.protein).put("fat_g",entry.fat).put("carbs_g",entry.carbs).put("user_reviewed",true))
    return client.request("food-entries/native","POST",JSONObject().put("consent",true).put("entries",body)).getJSONObject("data").getInt("count")
}
