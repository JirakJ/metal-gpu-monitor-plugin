package dev.htmeta.mgm.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import dev.htmeta.mgm.services.GPUHistoryService
import dev.htmeta.mgm.services.GPUMonitoringService
import dev.htmeta.mgm.settings.GPUSettingsState
import java.text.SimpleDateFormat
import java.util.*

class ExportGPUDataAction : AnAction() {
    private val LOG = thisLogger()

    override fun actionPerformed(e: AnActionEvent) {
        val format = GPUSettingsState.getInstance().state.exportFormat

        val extension = when (format) {
            GPUSettingsState.ExportFormat.JSON -> "json"
            GPUSettingsState.ExportFormat.CSV -> "csv"
        }
        val descriptor = FileSaverDescriptor(
            com.intellij.openapi.fileChooser.FileChooserDescriptor(true, false, false, false, false, false)
        ).withTitle("Export GPU Data")
        val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, e.project)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val wrapper = dialog.save("gpu-data-$timestamp.$extension") ?: return

        try {
            val file = wrapper.file
            val current = GPUMonitoringService.getInstance().latestSnapshot()
            val history = GPUHistoryService.getInstance().getFullHistory()
            when (format) {
                GPUSettingsState.ExportFormat.JSON -> file.writeText(
                    GPUDataExporter.toJson(current, history, SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()))
                )
                GPUSettingsState.ExportFormat.CSV -> file.writeText(GPUDataExporter.toCsv(current, history))
            }
            LOG.info("GPU data exported to ${file.absolutePath}")
        } catch (ex: Exception) {
            LOG.error("Failed to export GPU data", ex)
            com.intellij.openapi.ui.Messages.showErrorDialog(
                "Failed to export GPU data: ${ex.message}",
                "Export Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }
}
