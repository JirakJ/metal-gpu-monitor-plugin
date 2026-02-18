package dev.htmeta.mgm.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GPUDeviceInfoTest {

    @Test
    fun `UNKNOWN has correct defaults`() {
        val u = GPUDeviceInfo.UNKNOWN
        assertEquals("Unknown GPU", u.name)
        assertEquals(GPUVendor.UNKNOWN, u.vendor)
        assertEquals(0, u.coreCount)
        assertNull(u.maxClockMHz)
        assertEquals(0L, u.memoryTotalBytes)
        assertNull(u.metalFeatureSet)
    }

    @Test
    fun `sample has correct values`() {
        val s = GPUDeviceInfo.sample()
        assertEquals("Apple M3 Pro", s.name)
        assertEquals(GPUVendor.APPLE, s.vendor)
        assertEquals(18, s.coreCount)
        assertEquals(1398, s.maxClockMHz)
        assertEquals(38_654_705_664L, s.memoryTotalBytes)
        assertEquals("Metal 3", s.metalFeatureSet)
    }

    @Test
    fun `data class equality`() {
        val a = GPUDeviceInfo.sample()
        val b = GPUDeviceInfo.sample()
        assertEquals(a, b)
    }

    @Test
    fun `data class copy`() {
        val original = GPUDeviceInfo.sample()
        val copied = original.copy(name = "Apple M4 Max")
        assertEquals("Apple M4 Max", copied.name)
        assertEquals(original.vendor, copied.vendor)
    }

    @Test
    fun `custom construction`() {
        val info = GPUDeviceInfo("AMD Radeon", GPUVendor.AMD, 64, 2500, 16_000_000_000L, "Metal 2")
        assertEquals("AMD Radeon", info.name)
        assertEquals(GPUVendor.AMD, info.vendor)
        assertEquals(64, info.coreCount)
        assertEquals(2500, info.maxClockMHz)
        assertEquals(16_000_000_000L, info.memoryTotalBytes)
        assertEquals("Metal 2", info.metalFeatureSet)
    }
}
