package dev.htmeta.mgm.model

data class GPUSnapshot(
    val timestamp: Long,
    val gpuUtilization: Double,        // 0.0-1.0
    val gpuFrequencyMHz: Int?,
    val memoryAllocatedBytes: Long,
    val memoryTotalBytes: Long,
    val thermalState: ThermalState,
    val gpuPowerWatts: Double?,
    val renderUtilization: Double?,
    val computeUtilization: Double?
) {
    val memoryUtilization: Double
        get() = if (memoryTotalBytes > 0) memoryAllocatedBytes.toDouble() / memoryTotalBytes else 0.0

    companion object {
        val UNAVAILABLE = GPUSnapshot(
            timestamp = 0L,
            gpuUtilization = 0.0,
            gpuFrequencyMHz = null,
            memoryAllocatedBytes = 0,
            memoryTotalBytes = 0,
            thermalState = ThermalState.NOMINAL,
            gpuPowerWatts = null,
            renderUtilization = null,
            computeUtilization = null
        )
    }
}
