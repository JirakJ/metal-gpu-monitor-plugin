package dev.htmeta.mgm.ui.settings

import com.intellij.util.ui.JBUI
import dev.htmeta.mgm.settings.GPUSettingsState
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class GPUSettingsPanel : JPanel(GridBagLayout()) {

    private val refreshIntervalCombo = JComboBox(arrayOf(
        IntervalItem(1000L, "1 second"),
        IntervalItem(2000L, "2 seconds"),
        IntervalItem(5000L, "5 seconds"),
        IntervalItem(10000L, "10 seconds")
    ))

    private val statusBarFormatCombo = JComboBox(arrayOf(
        FormatItem(GPUSettingsState.StatusBarFormat.ICON_PERCENT, "Icon + %"),
        FormatItem(GPUSettingsState.StatusBarFormat.PERCENT_ONLY, "% only"),
        FormatItem(GPUSettingsState.StatusBarFormat.BAR, "Bar"),
        FormatItem(GPUSettingsState.StatusBarFormat.OFF, "Off")
    ))

    private val gpuThresholdSpinner = JSpinner(SpinnerNumberModel(80, 10, 100, 5)).apply {
        toolTipText = "GPU utilization alert threshold (%)"
    }

    private val memThresholdSpinner = JSpinner(SpinnerNumberModel(90, 10, 100, 5)).apply {
        toolTipText = "Memory utilization alert threshold (%)"
    }

    private val cooldownSpinner = JSpinner(SpinnerNumberModel(60, 10, 600, 10)).apply {
        toolTipText = "Alert cooldown in seconds"
    }

    private val alertsEnabledCheckBox = JCheckBox("Enable GPU alerts")

    var refreshIntervalMs: Long
        get() = (refreshIntervalCombo.selectedItem as? IntervalItem)?.ms ?: 1000L
        set(value) {
            for (i in 0 until refreshIntervalCombo.itemCount) {
                if (refreshIntervalCombo.getItemAt(i).ms == value) {
                    refreshIntervalCombo.selectedIndex = i
                    return
                }
            }
        }

    var statusBarFormat: GPUSettingsState.StatusBarFormat
        get() = (statusBarFormatCombo.selectedItem as? FormatItem)?.format
            ?: GPUSettingsState.StatusBarFormat.ICON_PERCENT
        set(value) {
            for (i in 0 until statusBarFormatCombo.itemCount) {
                if (statusBarFormatCombo.getItemAt(i).format == value) {
                    statusBarFormatCombo.selectedIndex = i
                    return
                }
            }
        }

    var gpuUtilizationThreshold: Double
        get() = (gpuThresholdSpinner.value as Int) / 100.0
        set(value) { gpuThresholdSpinner.value = (value * 100).toInt() }

    var memoryUtilizationThreshold: Double
        get() = (memThresholdSpinner.value as Int) / 100.0
        set(value) { memThresholdSpinner.value = (value * 100).toInt() }

    var alertCooldownSeconds: Int
        get() = cooldownSpinner.value as Int
        set(value) { cooldownSpinner.value = value }

    var alertsEnabled: Boolean
        get() = alertsEnabledCheckBox.isSelected
        set(value) { alertsEnabledCheckBox.isSelected = value }

    init {
        border = JBUI.Borders.empty(12)
        buildLayout()
    }

    private fun buildLayout() {
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(4, 0, 4, 8)
        }

        var row = 0

        // Section: General
        addSectionLabel("General", row++, gbc)
        addLabeledRow("Refresh interval:", refreshIntervalCombo, row++, gbc)
        addLabeledRow("Status bar format:", statusBarFormatCombo, row++, gbc)

        // Separator
        addSeparator(row++, gbc)

        // Section: Alerts
        addSectionLabel("Alerts", row++, gbc)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2
        add(alertsEnabledCheckBox, gbc)
        gbc.gridwidth = 1
        row++

        addLabeledRow("GPU threshold (%):", gpuThresholdSpinner, row++, gbc)
        addLabeledRow("Memory threshold (%):", memThresholdSpinner, row++, gbc)
        addLabeledRow("Alert cooldown (s):", cooldownSpinner, row++, gbc)

        // Filler
        gbc.gridx = 0; gbc.gridy = row; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.VERTICAL
        add(Box.createVerticalGlue(), gbc)
    }

    private fun addSectionLabel(text: String, row: Int, gbc: GridBagConstraints) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        val label = JLabel(text).apply {
            font = font.deriveFont(java.awt.Font.BOLD)
            border = JBUI.Borders.emptyBottom(4)
        }
        add(label, gbc)
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE
    }

    private fun addLabeledRow(labelText: String, component: JComponent, row: Int, gbc: GridBagConstraints) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0
        add(JLabel(labelText), gbc)
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL
        add(component, gbc)
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
    }

    private fun addSeparator(row: Int, gbc: GridBagConstraints) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL
        add(JSeparator(), gbc)
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE
    }

    private data class IntervalItem(val ms: Long, val label: String) {
        override fun toString(): String = label
    }

    private data class FormatItem(val format: GPUSettingsState.StatusBarFormat, val label: String) {
        override fun toString(): String = label
    }
}
