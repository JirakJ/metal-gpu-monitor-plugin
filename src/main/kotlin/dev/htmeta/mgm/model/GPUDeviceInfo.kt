package dev.htmeta.mgm.model

data class GPUDeviceInfo(
    val name: String,
    val vendor: GPUVendor,
    val coreCount: Int,
    val maxClockMHz: Int?,
    val memoryTotalBytes: Long,
    val metalFeatureSet: String?
) {
    companion object {
        val UNKNOWN = GPUDeviceInfo("Unknown GPU", GPUVendor.UNKNOWN, 0, null, 0, null)
    }
}
