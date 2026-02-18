package dev.htmeta.mgm.model

enum class AlertMetric(val label: String) {
    GPU_UTILIZATION("GPU Utilization"),
    MEMORY_UTILIZATION("Memory Utilization"),
    THERMAL_STATE("Thermal State")
}

enum class Comparison { ABOVE, BELOW }

data class AlertRule(
    val id: String,
    val metric: AlertMetric,
    val threshold: Double,
    val comparison: Comparison = Comparison.ABOVE,
    val cooldownSeconds: Int = 60,
    val enabled: Boolean = true
) {
    fun isTriggered(snapshot: GPUSnapshot): Boolean {
        if (!enabled) return false
        val value = when (metric) {
            AlertMetric.GPU_UTILIZATION -> snapshot.gpuUtilization
            AlertMetric.MEMORY_UTILIZATION -> snapshot.memoryUtilization
            AlertMetric.THERMAL_STATE -> snapshot.thermalState.level.toDouble() / ThermalState.CRITICAL.level
        }
        return when (comparison) {
            Comparison.ABOVE -> value > threshold
            Comparison.BELOW -> value < threshold
        }
    }
}
