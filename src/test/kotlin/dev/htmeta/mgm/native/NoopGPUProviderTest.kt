package dev.htmeta.mgm.native

import dev.htmeta.mgm.model.GPUDeviceInfo
import dev.htmeta.mgm.model.GPUSnapshot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NoopGPUProviderTest {

    private val provider = NoopGPUProvider()

    @Test
    fun `initialize returns false`() {
        assertFalse(provider.initialize())
    }

    @Test
    fun `fetchSnapshot returns UNAVAILABLE`() {
        assertEquals(GPUSnapshot.UNAVAILABLE, provider.fetchSnapshot())
    }

    @Test
    fun `fetchProcesses returns empty list`() {
        assertTrue(provider.fetchProcesses().isEmpty())
    }

    @Test
    fun `deviceInfo returns UNKNOWN`() {
        assertEquals(GPUDeviceInfo.UNKNOWN, provider.deviceInfo())
    }

    @Test
    fun `isAvailable returns false`() {
        assertFalse(provider.isAvailable())
    }

    @Test
    fun `cleanup does not throw`() {
        assertDoesNotThrow { provider.cleanup() }
    }

    @Test
    fun `implements GPUProvider interface`() {
        assertTrue(provider is GPUProvider)
    }
}
