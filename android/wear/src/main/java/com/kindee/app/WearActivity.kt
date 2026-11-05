package com.kindee.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.*
import com.google.android.gms.wearable.Wearable
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import org.json.JSONObject
import java.time.*

class WearActivity : Activity() {
    private val monitor by lazy { HealthServices.getClient(this).passiveMonitoringClient }
    private lateinit var status: TextView
    private var steps: Long? = null
    private var total: Double? = null
    private var day: String? = null
    private var observedAt: String? = null
    private var running = false
    private val listener = object : PassiveListenerCallback {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            val today = LocalDate.now().toString()
            if (day != today) { steps = null; total = null; day = today }
            val bootTime = Instant.now().minusMillis(android.os.SystemClock.elapsedRealtime())
            dataPoints.getData(DataType.STEPS_DAILY).lastOrNull()?.let {
                val timestamp = bootTime.plus(it.endDurationFromBoot)
                if (timestamp.atZone(ZoneId.systemDefault()).toLocalDate().toString() == today) {
                    steps = it.value; observedAt = timestamp.toString()
                }
            }
            dataPoints.getData(DataType.CALORIES_DAILY).lastOrNull()?.let {
                val timestamp = bootTime.plus(it.endDurationFromBoot)
                if (timestamp.atZone(ZoneId.systemDefault()).toLocalDate().toString() == today) {
                    total = it.value; observedAt = timestamp.toString()
                }
            }
            runOnUiThread { show() }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 24, 20, 24) }
        status = TextView(this).apply { text = "KinDee\nAllow activity access to read your watch. No readings yet." }
        layout.addView(status)
        layout.addView(Button(this).apply { text = "Allow & read"; setOnClickListener {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 10) else startReading()
        } })
        layout.addView(Button(this).apply { text = "Send to phone"; setOnClickListener { send() } })
        setContentView(ScrollView(this).apply { addView(layout) })
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) startReading()
        else status.text = "Activity permission not granted."
    }
    private fun startReading() {
        Futures.addCallback(monitor.getCapabilitiesAsync(), object : FutureCallback<PassiveMonitoringCapabilities> {
            override fun onSuccess(result: PassiveMonitoringCapabilities?) {
                val supported = result?.supportedDataTypesPassiveMonitoring.orEmpty()
                val types = setOf(DataType.STEPS_DAILY, DataType.CALORIES_DAILY).intersect(supported)
                if (types.isEmpty()) { status.text = "Daily monitoring unsupported on this watch."; return }
                try {
                    monitor.setPassiveListenerCallback(PassiveListenerConfig.builder().setDataTypes(types).build(), listener)
                    running = true
                    status.text = "Waiting for real watch readings. Keep this screen open."
                } catch (error: Exception) { status.text = "Unable to start monitoring." }
            }
            override fun onFailure(error: Throwable) { status.text = "Health Services unavailable. Wear OS 3+ required." }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun show() {
        status.text = "KinDee\nSteps: ${steps ?: "No data"}\nTotal energy: ${total?.toInt() ?: "No data"} kcal\nIncludes basal metabolism. Active energy is unknown."
    }
    private fun send() {
        if ((steps == null && total == null) || day != LocalDate.now().toString()) { status.text = "No current readings to send."; return }
        val payload = JSONObject().put("provider", "wear_os").put("date", day).put("timezone", ZoneId.systemDefault().id)
            .put("steps", steps ?: JSONObject.NULL).put("total_energy_kcal", total ?: JSONObject.NULL)
            .put("active_calories_kcal", JSONObject.NULL).put("collected_at", observedAt)
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) { status.text = "No paired Android phone connected."; return@addOnSuccessListener }
            nodes.forEach { node ->
                Wearable.getMessageClient(this).sendMessage(node.id, "/kindee/activity", payload.toString().toByteArray(Charsets.UTF_8))
                    .addOnSuccessListener { status.text = "Sent. Open KinDee on phone and import watch readings with consent." }
                    .addOnFailureListener { status.text = "Unable to send. Keep KinDee open on the paired phone." }
            }
        }.addOnFailureListener { status.text = "Unable to find paired phone." }
    }
    override fun onStop() {
        if (running) { monitor.clearPassiveListenerCallbackAsync(); running = false }
        super.onStop()
    }
}
