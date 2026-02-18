package dev.htmeta.mgm.native

import dev.htmeta.mgm.model.GPUDeviceInfo
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.model.ProcessGPUUsage

class NoopGPUProvider : GPUProvider {
    override fun initialize() = false
    override fun fetchSnapshot() = GPUSnapshot.UNAVAILABLE
    override fun fetchProcesses() = emptyList<ProcessGPUUsage>()
    override fun deviceInfo() = GPUDeviceInfo.UNKNOWN
    override fun cleanup() {}
    override fun isAvailable() = false
}
