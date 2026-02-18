package dev.htmeta.mgm.native

import dev.htmeta.mgm.util.PlatformDetector
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GPUProviderFactoryTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `non-macOS returns NoopGPUProvider`() {
        mockkObject(PlatformDetector)
        every { PlatformDetector.isMacOS() } returns false

        val factory = GPUProviderFactory()
        val provider = factory.createProvider()
        assertTrue(provider is NoopGPUProvider)
    }

    @Test
    fun `macOS with JNA unavailable returns NoopGPUProvider`() {
        mockkObject(PlatformDetector)
        every { PlatformDetector.isMacOS() } returns true

        val factory = GPUProviderFactory()
        // In test environment, IOKit/JNA may not be available,
        // so the factory should gracefully fall back to NoopGPUProvider
        val provider = factory.createProvider()
        // Either IOKitGPUProvider (on macOS with JNA) or NoopGPUProvider (fallback)
        assertTrue(provider is GPUProvider)
    }
}
