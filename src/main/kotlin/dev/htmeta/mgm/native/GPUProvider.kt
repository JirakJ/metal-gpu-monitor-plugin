package dev.htmeta.mgm.native

import dev.htmeta.mgm.model.GPUDeviceInfo
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.model.ProcessGPUUsage

interface GPUProvider {
    fun initialize(): Boolean
    fun fetchSnapshot(): GPUSnapshot
    fun fetchProcesses(): List<ProcessGPUUsage>
    fun deviceInfo(): GPUDeviceInfo
    fun cleanup()
    fun isAvailable(): Boolean
}
