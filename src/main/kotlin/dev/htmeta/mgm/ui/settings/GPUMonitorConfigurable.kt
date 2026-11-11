package dev.htmeta.mgm.ui.settings

import com.intellij.openapi.options.Configurable
import dev.htmeta.mgm.settings.GPUSettingsState
import javax.swing.JComponent

class GPUMonitorConfigurable : Configurable {

    private var settingsPanel: GPUSettingsPanel? = null

    override fun getDisplayName(): String = "GPU Monitor"

    override fun createComponent(): JComponent {
        val panel = GPUSettingsPanel()
        settingsPanel = panel
        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val panel = settingsPanel ?: return false
        val state = GPUSettingsState.getInstance().state
        return panel.refreshIntervalMs != state.refreshIntervalMs
                || panel.statusBarFormat != state.statusBarFormat
                || panel.gpuUtilizationThreshold != state.gpuUtilizationThreshold
                || panel.memoryUtilizationThreshold != state.memoryUtilizationThreshold
                || panel.alertCooldownSeconds != state.alertCooldownSeconds
                || panel.alertsEnabled != state.alertsEnabled
                || panel.exportFormat != state.exportFormat
    }

    override fun apply() {
        val panel = settingsPanel ?: return
        val state = GPUSettingsState.getInstance().state
        state.refreshIntervalMs = panel.refreshIntervalMs
        state.statusBarFormat = panel.statusBarFormat
        state.gpuUtilizationThreshold = panel.gpuUtilizationThreshold
        state.memoryUtilizationThreshold = panel.memoryUtilizationThreshold
        state.alertCooldownSeconds = panel.alertCooldownSeconds
        state.alertsEnabled = panel.alertsEnabled
        state.exportFormat = panel.exportFormat
    }

    override fun reset() {
        val panel = settingsPanel ?: return
        val state = GPUSettingsState.getInstance().state
        panel.refreshIntervalMs = state.refreshIntervalMs
        panel.statusBarFormat = state.statusBarFormat
        panel.gpuUtilizationThreshold = state.gpuUtilizationThreshold
        panel.memoryUtilizationThreshold = state.memoryUtilizationThreshold
        panel.alertCooldownSeconds = state.alertCooldownSeconds
        panel.alertsEnabled = state.alertsEnabled
        panel.exportFormat = state.exportFormat
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}
