package dev.htmeta.mgm.licensing

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LicenseCheckerTest {

    @Test
    fun `isProLicensed returns false when LicensingFacade not available`() {
        // In test environment, LicensingFacade.getInstance() returns null or class not found
        assertFalse(LicenseChecker.isProLicensed())
    }

    @Test
    fun `isProLicensed does not throw`() {
        assertDoesNotThrow { LicenseChecker.isProLicensed() }
    }
}
