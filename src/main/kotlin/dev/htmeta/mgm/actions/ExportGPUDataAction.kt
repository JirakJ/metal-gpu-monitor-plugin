package dev.htmeta.mgm.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import dev.htmeta.mgm.licensing.TierFeatureGate
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.services.GPUHistoryService
import dev.htmeta.mgm.services.GPUMonitoringService
import dev.htmeta.mgm.settings.GPUSettingsState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExportGPUDataAction : AnAction() {
    private val LOG = thisLogger()

    override fun actionPerformed(e: AnActionEvent) {
        val gate = TierFeatureGate.getInstance()
        val format = GPUSettingsState.getInstance().state.exportFormat
        val feature = when (format) {
            GPUSettingsState.ExportFormat.JSON -> TierFeatureGate.Feature.JSON_EXPORT
            GPUSettingsState.ExportFormat.CSV -> TierFeatureGate.Feature.CSV_EXPORT
        }
        if (!gate.isAvailable(feature)) {
            com.intellij.openapi.ui.Messages.showWarningDialog(
                "Data export is a Pro feature. Please upgrade your license.",
                "Pro Feature Required"
            )
            return
        }

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
                GPUSettingsState.ExportFormat.JSON -> exportJson(file, current, history)
                GPUSettingsState.ExportFormat.CSV -> exportCsv(file, current, history)
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
        e.presentation.isEnabled = TierFeatureGate.getInstance().isAvailable(TierFeatureGate.Feature.JSON_EXPORT)
    }

    private fun exportJson(file: File, current: GPUSnapshot, history: List<GPUSnapshot>) {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"exportTime\": \"${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date())}\",")
        sb.appendLine("  \"current\": ${snapshotToJson(current)},")
        sb.appendLine("  \"history\": [")
        history.forEachIndexed { index, snapshot ->
            val comma = if (index < history.size - 1) "," else ""
            sb.appendLine("    ${snapshotToJson(snapshot)}$comma")
        }
        sb.appendLine("  ]")
        sb.appendLine("}")
        file.writeText(sb.toString())
    }

    private fun snapshotToJson(s: GPUSnapshot): String {
        return buildString {
            append("{")
            append("\"timestamp\":${s.timestamp},")
            append("\"gpuUtilization\":${s.gpuUtilization},")
            append("\"gpuFrequencyMHz\":${s.gpuFrequencyMHz ?: "null"},")
            append("\"memoryAllocatedBytes\":${s.memoryAllocatedBytes},")
            append("\"memoryTotalBytes\":${s.memoryTotalBytes},")
            append("\"memoryUtilization\":${s.memoryUtilization},")
            append("\"thermalState\":\"${s.thermalState.name}\",")
            append("\"gpuPowerWatts\":${s.gpuPowerWatts ?: "null"},")
            append("\"renderUtilization\":${s.renderUtilization ?: "null"},")
            append("\"computeUtilization\":${s.computeUtilization ?: "null"}")
            append("}")
        }
    }

    private fun exportCsv(file: File, current: GPUSnapshot, history: List<GPUSnapshot>) {
        val header = "timestamp,gpuUtilization,gpuFrequencyMHz,memoryAllocatedBytes,memoryTotalBytes,memoryUtilization,thermalState,gpuPowerWatts,renderUtilization,computeUtilization"
        val rows = (history + current).joinToString("\n") { s ->
            "${s.timestamp},${s.gpuUtilization},${s.gpuFrequencyMHz ?: ""},${s.memoryAllocatedBytes},${s.memoryTotalBytes},${s.memoryUtilization},${s.thermalState.name},${s.gpuPowerWatts ?: ""},${s.renderUtilization ?: ""},${s.computeUtilization ?: ""}"
        }
        file.writeText("$header\n$rows\n")
    }
}
