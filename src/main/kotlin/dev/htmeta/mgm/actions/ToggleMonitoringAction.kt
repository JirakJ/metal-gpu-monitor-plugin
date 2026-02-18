package dev.htmeta.mgm.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import dev.htmeta.mgm.services.GPUMonitoringService

class ToggleMonitoringAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        GPUMonitoringService.getInstance().toggleMonitoring()
    }

    override fun update(e: AnActionEvent) {
        val isMonitoring = GPUMonitoringService.getInstance().isMonitoring
        e.presentation.text = if (isMonitoring) "Pause GPU Monitoring" else "Resume GPU Monitoring"
        e.presentation.description = if (isMonitoring) "Pause real-time GPU monitoring" else "Resume real-time GPU monitoring"
    }
}
