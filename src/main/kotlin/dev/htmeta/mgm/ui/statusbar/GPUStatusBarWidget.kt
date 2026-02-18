package dev.htmeta.mgm.ui.statusbar

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.Consumer
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.services.GPUMonitoringService
import kotlinx.coroutines.*

class GPUStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {

    private val LOG = thisLogger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var statusBar: StatusBar? = null

    @Volatile
    private var currentSnapshot: GPUSnapshot = GPUSnapshot.UNAVAILABLE

    override fun ID(): String = GPUStatusBarWidgetFactory.WIDGET_ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        scope.launch {
            GPUMonitoringService.getInstance().snapshots.collect { snapshot ->
                currentSnapshot = snapshot
                ApplicationManager.getApplication().invokeLater {
                    statusBar.updateWidget(ID())
                }
            }
        }
        LOG.info("GPU status bar widget installed")
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String {
        val pct = (currentSnapshot.gpuUtilization * 100).toInt()
        val icon = when {
            pct < 50 -> "\uD83D\uDFE2"  // 🟢
            pct <= 80 -> "\uD83D\uDFE1"  // 🟡
            else -> "\uD83D\uDD34"        // 🔴
        }
        return "$icon GPU $pct%"
    }

    override fun getTooltipText(): String {
        val s = currentSnapshot
        val gpuPct = (s.gpuUtilization * 100).toInt()
        val memPct = (s.memoryUtilization * 100).toInt()
        val memAllocMB = s.memoryAllocatedBytes / (1024 * 1024)
        val memTotalMB = s.memoryTotalBytes / (1024 * 1024)
        return buildString {
            append("GPU Utilization: $gpuPct%\n")
            append("Memory: $memAllocMB MB / $memTotalMB MB ($memPct%)\n")
            append("Thermal: ${s.thermalState.label}")
        }
    }

    override fun getAlignment(): Float = 0f

    override fun getClickConsumer(): Consumer<java.awt.event.MouseEvent> {
        return Consumer {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("GPU Monitor")
            toolWindow?.show()
        }
    }

    override fun dispose() {
        scope.cancel()
        statusBar = null
    }
}
