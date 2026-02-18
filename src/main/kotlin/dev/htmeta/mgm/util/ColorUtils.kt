package dev.htmeta.mgm.util

import com.intellij.ui.JBColor
import java.awt.Color

object ColorUtils {
    val GPU_GREEN = JBColor(Color(76, 175, 80), Color(76, 175, 80))
    val GPU_YELLOW = JBColor(Color(255, 193, 7), Color(255, 193, 7))
    val GPU_RED = JBColor(Color(244, 67, 54), Color(244, 67, 54))
    val GPU_CHART_FILL = JBColor(Color(76, 175, 80, 60), Color(76, 175, 80, 80))
    val GPU_CHART_LINE = JBColor(Color(76, 175, 80), Color(129, 199, 132))
    val MEMORY_LINE = JBColor(Color(33, 150, 243), Color(100, 181, 246))
    val GRID_LINE = JBColor(Color(200, 200, 200, 80), Color(80, 80, 80, 80))
    val TEXT_PRIMARY = JBColor.foreground()
    val TEXT_SECONDARY = JBColor.GRAY
    val BACKGROUND = JBColor.background()

    fun utilizationColor(utilization: Double): JBColor = when {
        utilization < 0.5 -> GPU_GREEN
        utilization < 0.8 -> GPU_YELLOW
        else -> GPU_RED
    }

    fun thermalColor(state: dev.htmeta.mgm.model.ThermalState): JBColor = when (state) {
        dev.htmeta.mgm.model.ThermalState.NOMINAL -> GPU_GREEN
        dev.htmeta.mgm.model.ThermalState.FAIR -> GPU_YELLOW
        dev.htmeta.mgm.model.ThermalState.SERIOUS -> GPU_RED
        dev.htmeta.mgm.model.ThermalState.CRITICAL -> GPU_RED
    }
}
