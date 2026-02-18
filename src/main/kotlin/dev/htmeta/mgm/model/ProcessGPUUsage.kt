package dev.htmeta.mgm.model

data class ProcessGPUUsage(
    val pid: Int,
    val processName: String,
    val gpuUtilization: Double,
    val gpuMemoryBytes: Long,
    val isIDEProcess: Boolean = false
)
