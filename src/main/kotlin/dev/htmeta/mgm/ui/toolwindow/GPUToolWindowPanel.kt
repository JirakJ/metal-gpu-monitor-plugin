package dev.htmeta.mgm.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.intellij.util.ui.JBUI
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.services.GPUMonitoringService
import dev.htmeta.mgm.settings.GPUSettingsState
import dev.htmeta.mgm.settings.RefreshIntervals
import java.awt.BorderLayout
import javax.swing.*

class GPUToolWindowPanel : JPanel(BorderLayout()), Disposable {

    private val LOG = thisLogger()

    private val infoPanel = GPUInfoPanel()
    private val chartPanel = GPUChartPanel()
    private val processTablePanel = ProcessTablePanel()

    private var updateTimer: Timer? = null
    private var updateCount = 0

    @Volatile
    private var isPaused = false

    init {
        LOG.info("GPUToolWindowPanel: initializing")

        val toolbar = createToolbar()

        // Wrap chart in a panel to ensure proper sizing
        val chartWrapper = JPanel(BorderLayout()).apply {
            add(chartPanel, BorderLayout.CENTER)
            minimumSize = java.awt.Dimension(JBUI.scale(200), JBUI.scale(100))
            preferredSize = java.awt.Dimension(JBUI.scale(400), JBUI.scale(200))
        }

        val splitter = JBSplitter(true, 0.6f).apply {
            firstComponent = chartWrapper
            secondComponent = processTablePanel
            dividerWidth = JBUI.scale(4)
        }

        val topSection = JPanel(BorderLayout()).apply {
            add(toolbar, BorderLayout.NORTH)
            add(infoPanel, BorderLayout.SOUTH)
        }
        add(topSection, BorderLayout.NORTH)
        add(splitter, BorderLayout.CENTER)

        LOG.info("GPUToolWindowPanel: layout complete, starting timer")

        // Show initial state immediately
        refreshData()

        // Start polling timer
        startTimer()

        Disposer.register(this, infoPanel)
        Disposer.register(this, chartPanel)
        Disposer.register(this, processTablePanel)

        LOG.info("GPUToolWindowPanel: initialization complete")
    }

    private fun createToolbar(): JPanel {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = JBUI.Borders.empty(4, 8)
        }

        val pauseButton = JButton("Pause").apply {
            addActionListener {
                isPaused = !isPaused
                text = if (isPaused) "Resume" else "Pause"
            }
        }

        val refreshCombo = JComboBox(RefreshIntervals.labels).apply {
            selectedItem = RefreshIntervals.labelFor(GPUSettingsState.getInstance().state.refreshIntervalMs)
            addActionListener {
                GPUSettingsState.getInstance().state.refreshIntervalMs = RefreshIntervals.msFor(selectedItem as? String)
                GPUMonitoringService.getInstance().restartMonitoring()
                restartTimer()
            }
        }

        val exportButton = JButton("Export").apply {
            toolTipText = "Export the current snapshot and history (format set in Settings | Tools | Metal GPU Monitor)"
        }
        exportButton.addActionListener {
            val action = ActionManager.getInstance().getAction(EXPORT_ACTION_ID)
            if (action != null) {
                ActionManager.getInstance().tryToExecute(action, null, exportButton, ActionPlaces.TOOLWINDOW_CONTENT, true)
            }
        }

        panel.add(pauseButton)
        panel.add(Box.createHorizontalStrut(JBUI.scale(8)))
        panel.add(JLabel("Refresh:"))
        panel.add(Box.createHorizontalStrut(JBUI.scale(4)))
        panel.add(refreshCombo)
        panel.add(Box.createHorizontalStrut(JBUI.scale(8)))
        panel.add(exportButton)
        panel.add(Box.createHorizontalGlue())

        return panel
    }

    private fun startTimer() {
        val interval = GPUSettingsState.getInstance().state.refreshIntervalMs.toInt()
        updateTimer = Timer(interval) {
            if (!isPaused) {
                refreshData()
            }
        }.apply {
            isRepeats = true
            start()
        }
        LOG.info("GPUToolWindowPanel: timer started with interval ${interval}ms")
    }

    private fun restartTimer() {
        updateTimer?.stop()
        startTimer()
    }

    private fun refreshData() {
        val service = GPUMonitoringService.getInstance()
        val snapshot = service.latestSnapshot()
        val processes = service.latestProcesses()

        updateCount++
        LOG.debug { "update #$updateCount GPU=${(snapshot.gpuUtilization * 100).toInt()}%" }

        infoPanel.updateSnapshot(snapshot, service.deviceInfo())
        chartPanel.addDataPoint(snapshot.gpuUtilization, snapshot.memoryUtilization)
        processTablePanel.updateProcesses(processes)
    }

    override fun dispose() {
        updateTimer?.stop()
        updateTimer = null
        LOG.info("GPUToolWindowPanel: disposed")
    }

    private companion object {
        const val EXPORT_ACTION_ID = "dev.htmeta.mgm.actions.ExportGPUData"
    }
}
