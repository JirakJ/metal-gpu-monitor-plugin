package dev.htmeta.mgm.licensing

import com.intellij.openapi.diagnostic.thisLogger

object LicenseChecker {
    private val LOG = thisLogger()
    private const val PRODUCT_CODE = "PMETALGPUMON"

    fun isProLicensed(): Boolean {
        return try {
            val facade = Class.forName("com.intellij.ui.LicensingFacade")
            val getInstance = facade.getMethod("getInstance")
            val instance = getInstance.invoke(null) ?: return false
            val confirmPaidPlugin = facade.getMethod("confirmPaidPluginLicense", String::class.java)
            confirmPaidPlugin.invoke(instance, PRODUCT_CODE) as? Boolean ?: false
        } catch (e: Exception) {
            LOG.debug("License check failed (expected on Community editions): ${e.message}")
            false
        }
    }
}
