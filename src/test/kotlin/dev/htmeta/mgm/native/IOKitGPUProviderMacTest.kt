/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.htmeta.mgm.native

import dev.htmeta.mgm.model.GPUSnapshot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

/**
 * Smoke test against the real IOKit/libproc APIs. Only meaningful on macOS — skipped elsewhere
 * (the logic it relies on is covered OS-independently by [GPUMetricsTest]).
 */
@EnabledOnOs(OS.MAC)
class IOKitGPUProviderMacTest {

    private val provider = IOKitGPUProvider()

    @AfterEach
    fun tearDown() = provider.cleanup()

    @Test
    fun `reads plausible values from the local GPU`() {
        assumeTrue(provider.initialize(), "no IOKit GPU accelerator service on this Mac (e.g. headless VM)")
        val snapshot = provider.fetchSnapshot()
        assertTrue(snapshot !== GPUSnapshot.UNAVAILABLE)
        assertTrue(snapshot.gpuUtilization in 0.0..1.0)
        assertTrue(snapshot.memoryAllocatedBytes >= 0)
        assertTrue(provider.deviceInfo().name.isNotBlank())
        val processes = provider.fetchProcesses()
        assertTrue(processes.any { it.isIDEProcess }, "the test JVM itself is always listed")
    }

    @Test
    fun `cleanup makes the provider unavailable`() {
        provider.initialize()
        provider.cleanup()
        assertTrue(!provider.isAvailable())
        assertTrue(provider.fetchSnapshot() === GPUSnapshot.UNAVAILABLE)
        assertTrue(provider.fetchProcesses().isEmpty())
    }
}
