package dev.htmeta.mgm.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GPUSnapshotTest {

    @Test
    fun `UNAVAILABLE has zero timestamp`() {
        assertEquals(0L, GPUSnapshot.UNAVAILABLE.timestamp)
    }

    @Test
    fun `UNAVAILABLE has zero utilization`() {
        assertEquals(0.0, GPUSnapshot.UNAVAILABLE.gpuUtilization)
    }

    @Test
    fun `UNAVAILABLE has null optional fields`() {
        assertNull(GPUSnapshot.UNAVAILABLE.gpuFrequencyMHz)
        assertNull(GPUSnapshot.UNAVAILABLE.gpuPowerWatts)
        assertNull(GPUSnapshot.UNAVAILABLE.renderUtilization)
        assertNull(GPUSnapshot.UNAVAILABLE.computeUtilization)
    }

    @Test
    fun `UNAVAILABLE has zero memory`() {
        assertEquals(0L, GPUSnapshot.UNAVAILABLE.memoryAllocatedBytes)
        assertEquals(0L, GPUSnapshot.UNAVAILABLE.memoryTotalBytes)
    }

    @Test
    fun `UNAVAILABLE has NOMINAL thermal state`() {
        assertEquals(ThermalState.NOMINAL, GPUSnapshot.UNAVAILABLE.thermalState)
    }

    @Test
    fun `sample uses default values`() {
        val s = GPUSnapshot.sample()
        assertEquals(0.42, s.gpuUtilization)
        assertEquals(1398, s.gpuFrequencyMHz)
        assertEquals(8_589_934_592L, s.memoryAllocatedBytes)
        assertEquals(38_654_705_664L, s.memoryTotalBytes)
        assertEquals(ThermalState.NOMINAL, s.thermalState)
        assertEquals(12.5, s.gpuPowerWatts)
        assertEquals(0.35, s.renderUtilization)
        assertEquals(0.07, s.computeUtilization)
    }

    @Test
    fun `sample with custom values`() {
        val s = GPUSnapshot.sample(
            timestamp = 123L,
            gpuUtilization = 0.9,
            gpuFrequencyMHz = 2000,
            memoryAllocatedBytes = 100L,
            memoryTotalBytes = 200L,
            thermalState = ThermalState.CRITICAL,
            gpuPowerWatts = 50.0,
            renderUtilization = 0.8,
            computeUtilization = 0.1
        )
        assertEquals(123L, s.timestamp)
        assertEquals(0.9, s.gpuUtilization)
        assertEquals(2000, s.gpuFrequencyMHz)
        assertEquals(100L, s.memoryAllocatedBytes)
        assertEquals(200L, s.memoryTotalBytes)
        assertEquals(ThermalState.CRITICAL, s.thermalState)
        assertEquals(50.0, s.gpuPowerWatts)
        assertEquals(0.8, s.renderUtilization)
        assertEquals(0.1, s.computeUtilization)
    }

    @Test
    fun `memoryUtilization normal case`() {
        val s = GPUSnapshot.sample(memoryAllocatedBytes = 50, memoryTotalBytes = 100)
        assertEquals(0.5, s.memoryUtilization, 0.0001)
    }

    @Test
    fun `memoryUtilization with zero total returns 0`() {
        val s = GPUSnapshot.sample(memoryAllocatedBytes = 50, memoryTotalBytes = 0)
        assertEquals(0.0, s.memoryUtilization)
    }

    @Test
    fun `memoryUtilization full usage`() {
        val s = GPUSnapshot.sample(memoryAllocatedBytes = 100, memoryTotalBytes = 100)
        assertEquals(1.0, s.memoryUtilization, 0.0001)
    }

    @Test
    fun `memoryUtilization zero allocated`() {
        val s = GPUSnapshot.sample(memoryAllocatedBytes = 0, memoryTotalBytes = 100)
        assertEquals(0.0, s.memoryUtilization, 0.0001)
    }

    @Test
    fun `data class equality`() {
        val ts = 1000L
        val a = GPUSnapshot.sample(timestamp = ts)
        val b = GPUSnapshot.sample(timestamp = ts)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `data class inequality`() {
        val a = GPUSnapshot.sample(timestamp = 1L)
        val b = GPUSnapshot.sample(timestamp = 2L)
        assertNotEquals(a, b)
    }

    @Test
    fun `data class copy`() {
        val original = GPUSnapshot.sample(timestamp = 100L)
        val copied = original.copy(gpuUtilization = 0.99)
        assertEquals(100L, copied.timestamp)
        assertEquals(0.99, copied.gpuUtilization)
        assertEquals(original.memoryAllocatedBytes, copied.memoryAllocatedBytes)
    }
}
