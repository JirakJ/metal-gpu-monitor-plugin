package dev.htmeta.mgm.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProcessGPUUsageTest {

    @Test
    fun `data class creation with all fields`() {
        val p = ProcessGPUUsage(
            pid = 1234,
            processName = "idea",
            gpuUtilization = 0.5,
            gpuMemoryBytes = 1024L,
            isIDEProcess = true
        )
        assertEquals(1234, p.pid)
        assertEquals("idea", p.processName)
        assertEquals(0.5, p.gpuUtilization)
        assertEquals(1024L, p.gpuMemoryBytes)
        assertTrue(p.isIDEProcess)
    }

    @Test
    fun `default isIDEProcess is false`() {
        val p = ProcessGPUUsage(
            pid = 1,
            processName = "chrome",
            gpuUtilization = 0.1,
            gpuMemoryBytes = 512L
        )
        assertFalse(p.isIDEProcess)
    }

    @Test
    fun `data class equality`() {
        val a = ProcessGPUUsage(1, "test", 0.5, 100L, false)
        val b = ProcessGPUUsage(1, "test", 0.5, 100L, false)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `data class inequality`() {
        val a = ProcessGPUUsage(1, "test", 0.5, 100L)
        val b = ProcessGPUUsage(2, "test", 0.5, 100L)
        assertNotEquals(a, b)
    }

    @Test
    fun `data class copy`() {
        val original = ProcessGPUUsage(1, "chrome", 0.5, 100L)
        val copied = original.copy(isIDEProcess = true)
        assertEquals(1, copied.pid)
        assertTrue(copied.isIDEProcess)
    }
}
