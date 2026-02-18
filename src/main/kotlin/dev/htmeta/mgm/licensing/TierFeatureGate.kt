package dev.htmeta.mgm.licensing

import com.intellij.openapi.components.service
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class TierFeatureGate {
    enum class Feature(val minimumTier: Tier) {
        FULL_PROCESS_LIST(Tier.PRO),
        HISTORICAL_CHARTS_24H(Tier.PRO),
        RUN_CONFIG_OVERLAY(Tier.PRO),
        ALERTS(Tier.PRO),
        JSON_EXPORT(Tier.PRO),
        CSV_EXPORT(Tier.PRO),
        STANDALONE_BRIDGE(Tier.PRO),
        CUSTOM_LAYOUT(Tier.PRO)
    }

    enum class Tier : Comparable<Tier> { FREE, PRO }

    fun currentTier(): Tier = if (LicenseChecker.isProLicensed()) Tier.PRO else Tier.FREE
    fun isAvailable(feature: Feature): Boolean = currentTier() >= feature.minimumTier

    companion object {
        fun getInstance(): TierFeatureGate = service<TierFeatureGate>()
    }
}
