package dev.htmeta.mgm.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.intellij.util.ui.JBUI
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.services.GPUMonitoringService
import dev.htmeta.mgm.settings.GPUSettingsState
import java.awt.BorderLayout
import javax.swing.*

class GPUToolWindowPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

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

        val refreshCombo = JComboBox(arrayOf("50ms", "100ms", "250ms", "500ms", "1s")).apply {
            val currentMs = GPUSettingsState.getInstance().state.refreshIntervalMs
            selectedItem = when (currentMs) {
                50L -> "50ms"
                100L -> "100ms"
                250L -> "250ms"
                500L -> "500ms"
                1000L -> "1s"
                else -> "100ms"
            }
            addActionListener {
                val ms = when (selectedItem as String) {
                    "50ms" -> 50L
                    "100ms" -> 100L
                    "250ms" -> 250L
                    "500ms" -> 500L
                    "1s" -> 1000L
                    else -> 100L
                }
                GPUSettingsState.getInstance().state.refreshIntervalMs = ms
                GPUMonitoringService.getInstance().restartMonitoring()
                restartTimer()
            }
        }

        val exportButton = JButton("Export").apply {
            addActionListener {
                LOG.info("Export requested")
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
        if (updateCount <= 5 || updateCount % 100 == 0) {
            LOG.info("GPUToolWindowPanel: update #$updateCount, GPU=${(snapshot.gpuUtilization * 100).toInt()}%, bufferSize=${chartPanel.bufferSize}")
        }

        infoPanel.updateSnapshot(snapshot, service.deviceInfo())
        chartPanel.addDataPoint(snapshot.gpuUtilization, snapshot.memoryUtilization)
        processTablePanel.updateProcesses(processes)
    }

    override fun dispose() {
        updateTimer?.stop()
        updateTimer = null
        LOG.info("GPUToolWindowPanel: disposed")
    }
}
