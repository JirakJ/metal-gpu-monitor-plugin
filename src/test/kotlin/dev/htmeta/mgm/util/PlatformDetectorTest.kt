package dev.htmeta.mgm.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlatformDetectorTest {

    @Test
    fun `isMacOS runs without error`() {
        // Just verify it doesn't throw — actual value depends on platform
        assertDoesNotThrow { PlatformDetector.isMacOS() }
    }

    @Test
    fun `isAppleSilicon runs without error`() {
        assertDoesNotThrow { PlatformDetector.isAppleSilicon() }
    }

    @Test
    fun `isIntelMac runs without error`() {
        assertDoesNotThrow { PlatformDetector.isIntelMac() }
    }

    @Test
    fun `cannot be both Apple Silicon and Intel Mac`() {
        val appleSilicon = PlatformDetector.isAppleSilicon()
        val intelMac = PlatformDetector.isIntelMac()
        assertFalse(appleSilicon && intelMac, "Cannot be both Apple Silicon and Intel Mac")
    }

    @Test
    fun `non-macOS implies not Apple Silicon and not Intel Mac`() {
        if (!PlatformDetector.isMacOS()) {
            assertFalse(PlatformDetector.isAppleSilicon())
            assertFalse(PlatformDetector.isIntelMac())
        }
    }
}
