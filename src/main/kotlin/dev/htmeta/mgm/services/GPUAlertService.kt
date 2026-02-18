package dev.htmeta.mgm.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.components.Service
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
    val alertLog: List<AlertEvent> get() = _alertLog.toList()
    
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
        if (!settings.alertsEnabled) return
        
        val rules = buildDefaultRules(settings)
        for (rule in rules) {
            if (rule.isTriggered(snapshot) && !isInCooldown(rule)) {
                fireAlert(rule, snapshot)
            }
        }
        
        // Always check thermal state
        if (snapshot.thermalState == ThermalState.SERIOUS || snapshot.thermalState == ThermalState.CRITICAL) {
            val thermalRule = AlertRule("thermal", AlertMetric.THERMAL_STATE, 0.5, Comparison.ABOVE, 120)
            if (!isInCooldown(thermalRule)) {
                fireAlert(thermalRule, snapshot)
            }
        }
    }
    
    private fun buildDefaultRules(settings: GPUSettingsState.State): List<AlertRule> {
        return listOf(
            AlertRule("gpu_high", AlertMetric.GPU_UTILIZATION, settings.gpuUtilizationThreshold, cooldownSeconds = settings.alertCooldownSeconds),
            AlertRule("mem_high", AlertMetric.MEMORY_UTILIZATION, settings.memoryUtilizationThreshold, cooldownSeconds = settings.alertCooldownSeconds)
        )
    }
    
    private fun isInCooldown(rule: AlertRule): Boolean {
        val lastTime = lastAlertTime[rule.id] ?: return false
        return System.currentTimeMillis() - lastTime < rule.cooldownSeconds * 1000L
    }
    
    private fun fireAlert(rule: AlertRule, snapshot: GPUSnapshot) {
        lastAlertTime[rule.id] = System.currentTimeMillis()
        val message = formatAlertMessage(rule, snapshot)
        
        _alertLog.add(AlertEvent(System.currentTimeMillis(), rule, snapshot, message))
        if (_alertLog.size > 100) _alertLog.removeFirst()
        
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("GPU Monitor Alerts")
                .createNotification(message, NotificationType.WARNING)
                .notify(null)
        } catch (e: Exception) {
            LOG.warn("Failed to send GPU alert notification", e)
        }
        
        LOG.info("GPU Alert: $message")
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
        fun getInstance(): GPUAlertService = service<GPUAlertService>()
    }
}
