package dev.htmeta.mgm.model

enum class ThermalState(val level: Int, val label: String) {
    NOMINAL(0, "Nominal"),
    FAIR(1, "Fair"),
    SERIOUS(2, "Serious"),
    CRITICAL(3, "Critical");

    companion object {
        fun fromNative(value: Int): ThermalState = entries.find { it.level == value } ?: NOMINAL
    }
}
