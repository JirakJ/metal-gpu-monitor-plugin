/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.htmeta.mgm.model

/** Test fixtures (kept out of the plugin binary). */
@Suppress("LongParameterList")
fun GPUSnapshot.Companion.sample(
    timestamp: Long = System.currentTimeMillis(),
    gpuUtilization: Double = 0.42,
    gpuFrequencyMHz: Int? = 1398,
    memoryAllocatedBytes: Long = 8_589_934_592L, // 8 GB
    memoryTotalBytes: Long = 38_654_705_664L, // 36 GB
    thermalState: ThermalState = ThermalState.NOMINAL,
    gpuPowerWatts: Double? = 12.5,
    renderUtilization: Double? = 0.35,
    computeUtilization: Double? = 0.07
) = GPUSnapshot(
    timestamp, gpuUtilization, gpuFrequencyMHz,
    memoryAllocatedBytes, memoryTotalBytes, thermalState,
    gpuPowerWatts, renderUtilization, computeUtilization
)

fun GPUDeviceInfo.Companion.sample() = GPUDeviceInfo("Apple M3 Pro", GPUVendor.APPLE, 18, 1398, 38_654_705_664L, "Metal 3")
