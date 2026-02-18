package dev.htmeta.mgm.licensing

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TierFeatureGateTest {

    private val gate = TierFeatureGate()

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `currentTier returns FREE when not licensed`() {
        mockkObject(LicenseChecker)
        every { LicenseChecker.isProLicensed() } returns false

        assertEquals(TierFeatureGate.Tier.FREE, gate.currentTier())
    }

    @Test
    fun `currentTier returns PRO when licensed`() {
        mockkObject(LicenseChecker)
        every { LicenseChecker.isProLicensed() } returns true

        assertEquals(TierFeatureGate.Tier.PRO, gate.currentTier())
    }

    @Test
    fun `isAvailable returns false for PRO features when on FREE tier`() {
        mockkObject(LicenseChecker)
        every { LicenseChecker.isProLicensed() } returns false

        assertFalse(gate.isAvailable(TierFeatureGate.Feature.FULL_PROCESS_LIST))
        assertFalse(gate.isAvailable(TierFeatureGate.Feature.JSON_EXPORT))
        assertFalse(gate.isAvailable(TierFeatureGate.Feature.CSV_EXPORT))
        assertFalse(gate.isAvailable(TierFeatureGate.Feature.ALERTS))
    }

    @Test
    fun `isAvailable returns true for PRO features when on PRO tier`() {
        mockkObject(LicenseChecker)
        every { LicenseChecker.isProLicensed() } returns true

        assertTrue(gate.isAvailable(TierFeatureGate.Feature.FULL_PROCESS_LIST))
        assertTrue(gate.isAvailable(TierFeatureGate.Feature.JSON_EXPORT))
        assertTrue(gate.isAvailable(TierFeatureGate.Feature.CSV_EXPORT))
        assertTrue(gate.isAvailable(TierFeatureGate.Feature.ALERTS))
        assertTrue(gate.isAvailable(TierFeatureGate.Feature.HISTORICAL_CHARTS_24H))
        assertTrue(gate.isAvailable(TierFeatureGate.Feature.RUN_CONFIG_OVERLAY))
        assertTrue(gate.isAvailable(TierFeatureGate.Feature.STANDALONE_BRIDGE))
        assertTrue(gate.isAvailable(TierFeatureGate.Feature.CUSTOM_LAYOUT))
    }

    @Test
    fun `all Feature enum values have PRO minimumTier`() {
        for (feature in TierFeatureGate.Feature.entries) {
            assertEquals(
                TierFeatureGate.Tier.PRO, feature.minimumTier,
                "Feature ${feature.name} should require PRO tier"
            )
        }
    }

    @Test
    fun `Tier ordering FREE is less than PRO`() {
        assertTrue(TierFeatureGate.Tier.FREE < TierFeatureGate.Tier.PRO)
    }

    @Test
    fun `Tier ordering PRO is not less than FREE`() {
        assertFalse(TierFeatureGate.Tier.PRO < TierFeatureGate.Tier.FREE)
    }

    @Test
    fun `Feature enum has expected count`() {
        assertEquals(8, TierFeatureGate.Feature.entries.size)
    }
}
