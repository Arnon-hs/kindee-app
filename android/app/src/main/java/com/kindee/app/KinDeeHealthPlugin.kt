package com.kindee.app

import androidx.activity.result.ActivityResult
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.getcapacitor.*
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import kotlinx.coroutines.*
import java.time.*
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.MessageClient
import org.json.JSONObject

@CapacitorPlugin(name = "KinDeeHealth")
class KinDeeHealthPlugin : Plugin() {
    private var pendingWear: JSONObject? = null
    private val wearListener = MessageClient.OnMessageReceivedListener { event ->
        if (event.path == "/kindee/activity" && event.data.size < 4096) {
            try { pendingWear = JSONObject(String(event.data, Charsets.UTF_8)).put("origin", "wear." + event.sourceNodeId) } catch (_: Exception) { }
        }
    }
    override fun load() { Wearable.getMessageClient(context).addListener(wearListener) }
    @PluginMethod fun readWearDaily(call: PluginCall) {
        val data = pendingWear
        if (data == null || data.optString("date") != call.getString("date") || data.optString("timezone") != call.getString("timezone")) {
            call.reject("health_no_current_wear_data"); return
        }
        call.resolve(JSObject(data.toString()))
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val permissions = setOf(HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class), HealthPermission.getReadPermission(StepsRecord::class))
    private fun available() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    private fun client() = HealthConnectClient.getOrCreate(context)
    private fun run(call: PluginCall, block: suspend () -> Unit) {
        scope.launch { try { if (!available()) call.reject("health_unavailable") else block() } catch (error: Exception) { call.reject("health_read_failed", error) } }
    }
    @PluginMethod fun availability(call: PluginCall) {
        call.resolve(JSObject().put("status", if (available()) "available" else "unavailable").put("provider", "health_connect"))
    }
    @PluginMethod fun authorize(call: PluginCall) {
        if (!available()) { call.reject("health_unavailable"); return }
        val intent = PermissionController.createRequestPermissionResultContract().createIntent(context, permissions)
        startActivityForResult(call, intent, "permissionsResult")
    }
    @ActivityCallback private fun permissionsResult(call: PluginCall?, result: ActivityResult) {
        if (call == null) return
        run(call) {
            val granted = client().permissionController.getGrantedPermissions()
            call.resolve(JSObject().put("status", if (granted.intersect(permissions).isEmpty()) "denied" else "authorized"))
        }
    }
    private fun range(call: PluginCall): TimeRangeFilter {
        val zone = ZoneId.of(call.getString("timezone") ?: "UTC")
        val date = LocalDate.parse(call.getString("date") ?: LocalDate.now(zone).toString())
        val start = date.atStartOfDay(zone).toInstant()
        val end = minOf(date.plusDays(1).atStartOfDay(zone).toInstant(), Instant.now())
        require(start.isBefore(end)) { "invalid_day" }
        return TimeRangeFilter.between(start, end)
    }
    @PluginMethod fun sources(call: PluginCall) = run(call) {
        val origins = mutableSetOf<String>()
        val granted = client().permissionController.getGrantedPermissions()
        val time = range(call)
        if (HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) in granted) {
            var page: String? = null
            var pages = 0
            do {
                val result = client().readRecords(ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, time, pageSize = 1000, pageToken = page))
                origins.addAll(result.records.map { it.metadata.dataOrigin.packageName }); page = result.pageToken
                require(++pages <= 100) { "too_many_records" }
            } while (page != null)
        }
        if (HealthPermission.getReadPermission(StepsRecord::class) in granted) {
            var page: String? = null
            var pages = 0
            do {
                val result = client().readRecords(ReadRecordsRequest(StepsRecord::class, time, pageSize = 1000, pageToken = page))
                origins.addAll(result.records.map { it.metadata.dataOrigin.packageName }); page = result.pageToken
                require(++pages <= 100) { "too_many_records" }
            } while (page != null)
        }
        call.resolve(JSObject().put("origins", JSArray(origins.sorted())))
    }
    @PluginMethod fun readDaily(call: PluginCall) = run(call) {
        val origin = requireNotNull(call.getString("origin"))
        val granted = client().permissionController.getGrantedPermissions()
        val metrics = buildSet {
            if (HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) in granted) add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
            if (HealthPermission.getReadPermission(StepsRecord::class) in granted) add(StepsRecord.COUNT_TOTAL)
        }
        require(metrics.isNotEmpty()) { "permission_required" }
        val result = client().aggregate(AggregateRequest(metrics, range(call), setOf(DataOrigin(origin))))
        val output = JSObject().put("provider", "health_connect").put("origin", origin)
            .put("active_calories_kcal", result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: org.json.JSONObject.NULL)
            .put("steps", result[StepsRecord.COUNT_TOTAL] ?: org.json.JSONObject.NULL)
            .put("total_energy_kcal", org.json.JSONObject.NULL).put("collected_at", Instant.now().toString())
        call.resolve(output)
    }
    @PluginMethod fun clearSession(call: PluginCall) { pendingWear = null; call.resolve() }
    @PluginMethod fun disconnect(call: PluginCall) {
        pendingWear = null
        if (!available()) { call.resolve(); return }
        run(call) {
        client().permissionController.revokeAllPermissions()
        call.resolve()
        }
    }
    override fun handleOnDestroy() { pendingWear = null; Wearable.getMessageClient(context).removeListener(wearListener); scope.cancel() }
}
