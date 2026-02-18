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

        fun sample(
            timestamp: Long = System.currentTimeMillis(),
            gpuUtilization: Double = 0.42,
            gpuFrequencyMHz: Int? = 1398,
            memoryAllocatedBytes: Long = 8_589_934_592L,    // 8 GB
            memoryTotalBytes: Long = 38_654_705_664L,        // 36 GB
            thermalState: ThermalState = ThermalState.NOMINAL,
            gpuPowerWatts: Double? = 12.5,
            renderUtilization: Double? = 0.35,
            computeUtilization: Double? = 0.07
        ) = GPUSnapshot(
            timestamp, gpuUtilization, gpuFrequencyMHz,
            memoryAllocatedBytes, memoryTotalBytes, thermalState,
            gpuPowerWatts, renderUtilization, computeUtilization
        )
    }
}
