package dev.htmeta.mgm.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "GPUMonitorSettings", storages = [Storage("gpu-monitor-settings.xml")])
@Service(Service.Level.APP)
class GPUSettingsState : PersistentStateComponent<GPUSettingsState.State> {

    data class State(
        var refreshIntervalMs: Long = 1000,
        var statusBarEnabled: Boolean = true,
        var statusBarFormat: StatusBarFormat = StatusBarFormat.ICON_PERCENT,
        var showInToolWindow: Boolean = true,
        var historyRetentionMinutes: Int = 5,          // Free tier: 5 min
        var proHistoryRetentionMinutes: Int = 1440,     // Pro tier: 24 hours
        var alertsEnabled: Boolean = true,
        var gpuUtilizationThreshold: Double = 0.8,
        var memoryUtilizationThreshold: Double = 0.9,
        var alertCooldownSeconds: Int = 60,
        var exportFormat: ExportFormat = ExportFormat.JSON
    )

    enum class StatusBarFormat { ICON_PERCENT, PERCENT_ONLY, BAR, OFF }
    enum class ExportFormat { JSON, CSV }

    private var state = State()

    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }

    companion object {
        fun getInstance(): GPUSettingsState = service<GPUSettingsState>()
    }
}
