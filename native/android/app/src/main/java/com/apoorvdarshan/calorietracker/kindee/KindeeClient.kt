package com.apoorvdarshan.calorietracker.kindee

import android.content.Context
import com.apoorvdarshan.calorietracker.BuildConfig
import com.apoorvdarshan.calorietracker.data.KeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

internal fun validKindeeBase(value: String, localAllowed: Boolean): String {
    val u = value.trim().trimEnd('/').toHttpUrlOrNull() ?: error("Enter a valid server address")
    require(u.username.isEmpty() && u.password.isEmpty() && u.query == null && u.fragment == null) { "Invalid server address" }
    require(u.isHttps || (localAllowed && u.scheme == "http" && u.host == "127.0.0.1")) { "Use HTTPS for your server" }
    require(u.encodedPath.trimEnd('/') == "/api/v1") { "Server address must end with /api/v1" }
    return u.toString().trimEnd('/')
}

internal data class FoodPlace(val label: String, val latitude: Double, val longitude: Double, val source: String = "address")
internal val Homa = FoodPlace("Homa Phuket Town, 3/41 Soi Samkong 1", 7.9080136, 98.3785166, "manual")
internal data class PlanDraft(val from: String, val hash: String, val entries: List<JSONObject>, val requestID: String = UUID.randomUUID().toString())
internal fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }

internal class KindeeClient(context: Context, private val keys: KeyStore) {
    private val prefs = context.getSharedPreferences("kindee.server", Context.MODE_PRIVATE)
    private val http = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
        .connectTimeout(12, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).callTimeout(40, TimeUnit.SECONDS).build()
    var base: String
        get() = prefs.getString("base", "https://kindee-api.158.69.190.195.sslip.io/api/v1") ?: ""
        private set(v) { prefs.edit().putString("base", v).apply() }
    private fun tokenKey(address: String = base) = "kindee.token." + address
    val connected: Boolean get() = !keys.load(tokenKey()).isNullOrBlank()

    suspend fun login(address: String, email: String, password: String) {
        val next = validKindeeBase(address, BuildConfig.DEBUG)
        val result = request("auth/login", "POST", JSONObject().put("email", email.trim()).put("password", password), authenticated = false, address = next)
        val token = result.getJSONObject("data").getString("token")
        require(token.length in 20..1024) { "Invalid session response" }
        base = next
        keys.save(tokenKey(), token)
    }

    suspend fun request(path: String, method: String = "GET", body: JSONObject? = null, authenticated: Boolean = true, address: String = base): JSONObject = withContext(Dispatchers.IO) {
        val checked = validKindeeBase(address, BuildConfig.DEBUG)
        require(!path.startsWith('/') && !path.contains("://") && !path.contains(".."))
        val builder = Request.Builder().url("$checked/$path").header("Accept", "application/json")
        if (authenticated) builder.header("Authorization", "Bearer " + (keys.load(tokenKey(checked)) ?: error("Sign in to Kindee")))
        if (method != "GET") builder.method(method, (body ?: JSONObject()).toString().toRequestBody("application/json".toMediaType()))
        (if (path == "meal-plans/menu-nutrition") http.newBuilder().readTimeout(70, TimeUnit.SECONDS).callTimeout(75, TimeUnit.SECONDS).build() else http).newCall(builder.build()).execute().use { response ->
            if (response.code == 401) {
                if (authenticated) keys.delete(tokenKey(checked))
                error(if (authenticated) "Your session expired. Sign in again." else "Check your email and password.")
            }
            if (!response.isSuccessful) error(when(response.code) {409 -> "The menu changed. Create a fresh preview."; 422 -> "Check the address, diet and menu estimates. Restricted diets need verified ingredients."; 402 -> "Kindee AI is unavailable or its request limit was reached. Turn off menu estimates to build a cooking plan."; 429 -> "Please wait before trying again."; else -> "Kindee server is unavailable (${response.code})."})
            if (response.code == 204) return@withContext JSONObject()
            val source = response.body?.source() ?: error("Empty server response")
            if (source.request(4L * 1024 * 1024 + 1)) error("Server response is too large")
            JSONObject(source.readUtf8())
        }
    }

    suspend fun profile(): JSONObject = request("onboarding").getJSONObject("data")
    var menuStatus: String = "Saved menus · Confirm availability"
        private set
    suspend fun nearby(place: FoodPlace): List<JSONObject> {
        val query = okhttp3.HttpUrl.Builder().scheme("https").host("kindee.invalid")
            .addQueryParameter("lat",place.latitude.toString()).addQueryParameter("lng",place.longitude.toString())
            .addQueryParameter("radius_m","5000").addQueryParameter("q",place.label.take(200)).build().encodedQuery
        val result = request("meal-plans/nearby-menu?$query")
        val state = result.optJSONObject("meta")?.optJSONObject("providers")?.optJSONObject("line_man")?.optString("status")
        menuStatus = when(state) {
            "updated", "partial" -> "LINE MAN · Auto refresh · Check delivery"
            "unavailable" -> "Menu refresh unavailable · Showing saved menus"
            "no_coverage" -> "No new menus here · Saved menus only"
            "rate_limited", "refreshing" -> "Menu update pending · Try Refresh plan shortly"
            else -> "Saved menus · Confirm availability"
        }
        return result.getJSONArray("data").objects()
    }
    suspend fun search(query: String): List<FoodPlace> = request("meal-plans/location/search", "POST", JSONObject().put("query", query)).getJSONArray("data").objects().map { FoodPlace(it.getString("label"), it.getDouble("latitude"), it.getDouble("longitude")) }
    suspend fun savedPlan(): List<JSONObject> = request("meal-plans").getJSONObject("data").getJSONArray("entries").objects()
    suspend fun updateLocation(profile: JSONObject, place: FoodPlace, includeUnverified: Boolean, preferences: JSONObject? = null, goalKind: String? = null) {
        val body = JSONObject()
        for (key in listOf("goal_kind", "height_cm", "baseline_kg", "target_kg", "activity_level", "health_preference")) {
            if (profile.has(key)) body.put(key, profile.get(key))
        }
        val food = JSONObject(preferences?.toString() ?: profile.optJSONObject("food_preferences")?.toString() ?: "{}")
        food.put("location", JSONObject().put("city", place.label).put("latitude",place.latitude).put("longitude",place.longitude).put("radius_km",5).put("source",place.source))
        if (!food.has("diet")) food.put("diet","any")
        if (!food.has("likes")) food.put("likes",JSONArray())
        if (!food.has("excluded")) food.put("excluded",JSONArray())
        if (!food.has("meals_per_day")) food.put("meals_per_day",3)
        if (!food.has("cooking_minutes")) food.put("cooking_minutes",30)
        if (!food.has("budget_per_meal")) food.put("budget_per_meal",350)
        food.put("meal_mode","mixed").put("allow_unverified_menu",includeUnverified)
        if (goalKind != null) body.put("goal_kind",goalKind)
        body.put("food_preferences",food).put("consent",true)
        request("onboarding", "PUT", body)
    }
    suspend fun estimateMenus(): Int = request("meal-plans/menu-nutrition","POST",JSONObject().put("consent",true)).getJSONObject("data").getInt("ready")
    suspend fun preview(from: String): PlanDraft {
        val data = request("meal-plans/personalized/preview","POST",JSONObject().put("from",from)).getJSONObject("data")
        return PlanDraft(data.getString("from"),data.getString("preview_hash"),data.getJSONArray("entries").objects())
    }
    suspend fun save(draft: PlanDraft) {
        request("meal-plans/personalized","POST",JSONObject().put("from",draft.from).put("preview_hash",draft.hash).put("request_id",draft.requestID).put("replace_generated",true))
    }
    suspend fun connectFoodAi(preferences: com.apoorvdarshan.calorietracker.data.PreferencesStore) {
        val status = request("ai/status").getJSONObject("data")
        check(status.getBoolean("configured") && status.getInt("remaining") > 0) { "Kindee AI is unavailable or its request limit has been reached." }
        val token = keys.load(tokenKey()) ?: error("Sign in to Kindee")
        keys.setApiKey(com.apoorvdarshan.calorietracker.models.AIProvider.CUSTOM_OPENAI, token)
        preferences.configureKindeeFoodAi("$base/ai", status.getString("model"))
    }

    suspend fun logout() {
        val token = keys.load(tokenKey())
        try { request("auth/logout","POST") } finally {
            if (keys.apiKey(com.apoorvdarshan.calorietracker.models.AIProvider.CUSTOM_OPENAI) == token) {
                keys.setApiKey(com.apoorvdarshan.calorietracker.models.AIProvider.CUSTOM_OPENAI, null)
            }
            keys.delete(tokenKey())
        }
    }
}
