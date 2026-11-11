package dev.htmeta.mgm.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import dev.htmeta.mgm.model.*
import dev.htmeta.mgm.settings.GPUSettingsState
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.APP)
class GPUAlertService : Disposable {
    private val LOG = thisLogger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lastAlertTime = ConcurrentHashMap<String, Long>()
    private val _alertLog = mutableListOf<AlertEvent>()
    val alertLog: List<AlertEvent> get() = synchronized(_alertLog) { _alertLog.toList() }
    
    private var monitorJob: Job? = null
    
    data class AlertEvent(
        val timestamp: Long,
        val rule: AlertRule,
        val snapshot: GPUSnapshot,
        val message: String
    )
    
    fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            GPUMonitoringService.getInstance().snapshots.collect { snapshot ->
                checkAlerts(snapshot)
            }
        }
    }
    
    private fun checkAlerts(snapshot: GPUSnapshot) {
        val settings = GPUSettingsState.getInstance().state
        dueRules(snapshot, settings, lastAlertTime, System.currentTimeMillis()).forEach { fireAlert(it, snapshot) }
    }

    private fun fireAlert(rule: AlertRule, snapshot: GPUSnapshot) {
        lastAlertTime[rule.id] = System.currentTimeMillis()
        val message = formatAlertMessage(rule, snapshot)
        
        synchronized(_alertLog) {
            _alertLog.add(AlertEvent(System.currentTimeMillis(), rule, snapshot, message))
            if (_alertLog.size > MAX_ALERT_LOG) _alertLog.removeAt(0)
        }
        
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("GPU Monitor Alerts")
                .createNotification(message, NotificationType.WARNING)
                .notify(null)
        } catch (e: Exception) {
            LOG.warn("Failed to send GPU alert notification", e)
        }
        
        LOG.debug { "GPU Alert: $message" }
    }
    
    internal fun formatAlertMessage(rule: AlertRule, snapshot: GPUSnapshot): String {
        return when (rule.metric) {
            AlertMetric.GPU_UTILIZATION -> {
                val pct = (snapshot.gpuUtilization * 100).toInt()
                "GPU utilization is at $pct% (threshold: ${(rule.threshold * 100).toInt()}%)"
            }
            AlertMetric.MEMORY_UTILIZATION -> {
                val pct = (snapshot.memoryUtilization * 100).toInt()
                "GPU memory usage is at $pct% (threshold: ${(rule.threshold * 100).toInt()}%)"
            }
            AlertMetric.THERMAL_STATE -> {
                "GPU thermal state: ${snapshot.thermalState.label}"
            }
        }
    }
    
    fun clearAlertLog() {
        _alertLog.clear()
        lastAlertTime.clear()
    }
    
    override fun dispose() {
        monitorJob?.cancel()
        scope.cancel()
    }
    
    companion object {
        private const val MAX_ALERT_LOG = 100
        private const val THERMAL_COOLDOWN_SECONDS = 120

        fun getInstance(): GPUAlertService = service<GPUAlertService>()

        /** Threshold rules configured in the settings. */
        internal fun rulesFor(settings: GPUSettingsState.State): List<AlertRule> = listOf(
            AlertRule(
                "gpu_high", AlertMetric.GPU_UTILIZATION, settings.gpuUtilizationThreshold,
                cooldownSeconds = settings.alertCooldownSeconds
            ),
            AlertRule(
                "mem_high", AlertMetric.MEMORY_UTILIZATION, settings.memoryUtilizationThreshold,
                cooldownSeconds = settings.alertCooldownSeconds
            )
        )

        /** Whether [rule] fired less than its cooldown ago. */
        internal fun isInCooldown(rule: AlertRule, lastAlertTime: Map<String, Long>, now: Long): Boolean {
            val last = lastAlertTime[rule.id] ?: return false
            return now - last < rule.cooldownSeconds * 1000L
        }

        /**
         * Rules that should fire for [snapshot]: triggered threshold rules (when alerts are enabled) plus a
         * thermal alert for SERIOUS/CRITICAL states, each outside its cooldown.
         */
        internal fun dueRules(
            snapshot: GPUSnapshot,
            settings: GPUSettingsState.State,
            lastAlertTime: Map<String, Long>,
            now: Long
        ): List<AlertRule> {
            val thresholdRules = if (settings.alertsEnabled) rulesFor(settings).filter { it.isTriggered(snapshot) } else emptyList()
            val thermal = snapshot.thermalState == ThermalState.SERIOUS || snapshot.thermalState == ThermalState.CRITICAL
            val thermalRules = if (settings.alertsEnabled && thermal) {
                listOf(AlertRule("thermal", AlertMetric.THERMAL_STATE, 0.5, Comparison.ABOVE, THERMAL_COOLDOWN_SECONDS))
            } else {
                emptyList()
            }
            return (thresholdRules + thermalRules).filter { !isInCooldown(it, lastAlertTime, now) }
        }
    }
}
