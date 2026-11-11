package dev.htmeta.mgm.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import dev.htmeta.mgm.model.GPUDeviceInfo
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.model.ThermalState
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.JProgressBar

class GPUInfoPanel : JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(12), JBUI.scale(4))), Disposable {

    private val gpuNameLabel = JBLabel("GPU: —")
    private val thermalLabel = JBLabel("Thermal: —")
    private val memoryBar = JProgressBar(0, 100).apply {
        isStringPainted = true
        string = "Memory: —"
        preferredSize = java.awt.Dimension(JBUI.scale(200), JBUI.scale(16))
    }

    init {
        border = JBUI.Borders.empty(4, 8, 4, 8)
        add(gpuNameLabel)
        add(thermalLabel)
        add(memoryBar)
    }

    fun updateSnapshot(snapshot: GPUSnapshot, deviceInfo: GPUDeviceInfo) {
        val coreSuffix = if (deviceInfo.coreCount > 0) " (${deviceInfo.coreCount}-core)" else ""
        gpuNameLabel.text = "GPU: ${deviceInfo.name}$coreSuffix"

        thermalLabel.text = "Thermal: ${snapshot.thermalState.label}"
        thermalLabel.foreground = when (snapshot.thermalState) {
            ThermalState.NOMINAL -> JBColor(java.awt.Color(0x2E7D32), java.awt.Color(0x66BB6A))
            ThermalState.FAIR -> JBColor(java.awt.Color(0xF57F17), java.awt.Color(0xFFEE58))
            ThermalState.SERIOUS -> JBColor(java.awt.Color(0xE65100), java.awt.Color(0xFFA726))
            ThermalState.CRITICAL -> JBColor(java.awt.Color(0xB71C1C), java.awt.Color(0xEF5350))
        }

        val allocMB = snapshot.memoryAllocatedBytes / (1024 * 1024)
        val totalMB = snapshot.memoryTotalBytes / (1024 * 1024)
        val pct = if (snapshot.memoryTotalBytes > 0) {
            (snapshot.memoryAllocatedBytes * 100 / snapshot.memoryTotalBytes).toInt()
        } else 0
        memoryBar.value = pct
        memoryBar.string = "Memory: $allocMB / $totalMB MB ($pct%)"
    }

    override fun dispose() {
        // Nothing to release: the panel only renders device information.
    }
}
