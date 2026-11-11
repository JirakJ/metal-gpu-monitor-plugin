/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.htmeta.mgm.native

import dev.htmeta.mgm.model.GPUVendor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Platform-independent logic behind the IOKit provider — runs on every OS. */
class GPUMetricsTest {

    @Test
    fun `percent values are normalised from either scale`() {
        assertEquals(0.42, GPUMetrics.normalizePercent(42.0), 1e-9)
        assertEquals(0.42, GPUMetrics.normalizePercent(0.42), 1e-9)
        assertEquals(1.0, GPUMetrics.normalizePercent(150.0), 1e-9)
        assertEquals(0.0, GPUMetrics.normalizePercent(-3.0), 1e-9)
        assertEquals(1.0, GPUMetrics.normalizePercent(1.0), 1e-9)
    }

    @Test
    fun `power above 1000 is milliwatts`() {
        assertEquals(12.5, GPUMetrics.normalizePower(12_500.0)!!, 1e-9)
        assertEquals(12.5, GPUMetrics.normalizePower(12.5)!!, 1e-9)
        assertEquals(1000.0, GPUMetrics.normalizePower(1000.0)!!, 1e-9)
        assertNull(GPUMetrics.normalizePower(null))
    }

    @Test
    fun `megabyte keys are converted to bytes`() {
        assertEquals(8L * 1024 * 1024 * 1024, GPUMetrics.memoryBytes("VRAM,totalMB", 8192))
        assertEquals(8192L, GPUMetrics.memoryBytes("vramTotalBytes", 8192))
    }

    @Test
    fun `sysctl memsize output is parsed`() {
        assertEquals(38_654_705_664L, GPUMetrics.parseMemSize("38654705664\n"))
        assertEquals(0L, GPUMetrics.parseMemSize(""))
        assertEquals(0L, GPUMetrics.parseMemSize("sysctl: unknown oid"))
    }

    @Test
    fun `vendor detection`() {
        assertEquals(GPUVendor.APPLE, GPUMetrics.detectVendor("Apple M3 Pro"))
        assertEquals(GPUVendor.APPLE, GPUMetrics.detectVendor("AGXAcceleratorG15X"))
        assertEquals(GPUVendor.APPLE, GPUMetrics.detectVendor("m2"))
        assertEquals(GPUVendor.AMD, GPUMetrics.detectVendor("AMD Radeon Pro 5500M"))
        assertEquals(GPUVendor.INTEL, GPUMetrics.detectVendor("Intel Iris Plus Graphics"))
        assertEquals(GPUVendor.UNKNOWN, GPUMetrics.detectVendor("Unknown GPU"))
        // "m1" inside another word is not an Apple chip
        assertEquals(GPUVendor.UNKNOWN, GPUMetrics.detectVendor("Fermi1x"))
    }

    @Test
    fun `metal feature set per vendor`() {
        assertEquals("Metal 3", GPUMetrics.metalFeatureSet(GPUVendor.APPLE))
        assertEquals("Metal 2", GPUMetrics.metalFeatureSet(GPUVendor.AMD))
        assertEquals("Metal 1", GPUMetrics.metalFeatureSet(GPUVendor.INTEL))
        assertNull(GPUMetrics.metalFeatureSet(GPUVendor.UNKNOWN))
    }

    @Test
    fun `gpu process detection uses whole words for short patterns`() {
        assertTrue(GPUMetrics.isLikelyGpuProcess("Google Chrome Helper (GPU)"))
        assertTrue(GPUMetrics.isLikelyGpuProcess("Arc"))
        assertTrue(GPUMetrics.isLikelyGpuProcess("idea"))
        assertTrue(GPUMetrics.isLikelyGpuProcess("MTLCompilerService"))
        assertFalse(GPUMetrics.isLikelyGpuProcess("mdsearch"), "'arc' must not match inside 'search'")
        assertFalse(GPUMetrics.isLikelyGpuProcess("archiveutility"))
        assertFalse(GPUMetrics.isLikelyGpuProcess("knowledge-agent"), "'edge' must not match inside 'knowledge'")
        assertFalse(GPUMetrics.isLikelyGpuProcess("launchd"))
    }

    @Test
    fun `process selection keeps IDE, WindowServer and GPU users in priority order`() {
        val selected = GPUMetrics.selectGpuProcesses(
            listOf(
                1 to "launchd",
                10 to "Safari",
                0 to "kernel_task",
                20 to "WindowServer",
                30 to "",
                42 to "java",
            ),
            idePid = 42
        )
        assertEquals(listOf("java", "WindowServer", "Safari"), selected.map { it.processName })
        assertTrue(selected.first().isIDEProcess)
        assertTrue(selected.all { it.gpuUtilization == 0.0 && it.gpuMemoryBytes == 0L })
    }

    @Test
    fun `process selection of an empty list is empty`() {
        assertTrue(GPUMetrics.selectGpuProcesses(emptyList(), 1).isEmpty())
    }
}
