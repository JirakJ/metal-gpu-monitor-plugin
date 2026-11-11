package dev.htmeta.mgm.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JPanel

class GPUChartPanel : JPanel(), Disposable {

    private val LOG = thisLogger()
    private val gpuBuffer = ArrayDeque<Double>(MAX_POINTS)
    private val memBuffer = ArrayDeque<Double>(MAX_POINTS)
    private var dataPointCount = 0

    val bufferSize: Int get() = gpuBuffer.size

    private val gridColor = JBColor(Color(0xE0E0E0), Color(0x3C3F41))
    private val textColor = JBColor(Color(0x333333), Color(0xBBBBBB))
    private val memLineColor = JBColor(Color(0x1565C0), Color(0x42A5F5))

    init {
        border = JBUI.Borders.empty(8)
        preferredSize = Dimension(JBUI.scale(400), JBUI.scale(200))
        minimumSize = Dimension(JBUI.scale(200), JBUI.scale(100))
        isOpaque = true
        isDoubleBuffered = true
    }

    fun addDataPoint(gpuUtilization: Double, memoryUtilization: Double) {
        synchronized(gpuBuffer) {
            if (gpuBuffer.size >= MAX_POINTS) gpuBuffer.removeFirst()
            gpuBuffer.addLast(gpuUtilization.coerceIn(0.0, 1.0))
            if (memBuffer.size >= MAX_POINTS) memBuffer.removeFirst()
            memBuffer.addLast(memoryUtilization.coerceIn(0.0, 1.0))
        }
        dataPointCount++
        LOG.debug { "addDataPoint #$dataPointCount GPU=${(gpuUtilization * 100).toInt()}% MEM=${(memoryUtilization * 100).toInt()}%" }
        // Force repaint on EDT
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            revalidate()
            repaint()
        } else {
            javax.swing.SwingUtilities.invokeLater {
                revalidate()
                repaint()
            }
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Fill background
        g2.color = background ?: JBColor.PanelBackground
        g2.fillRect(0, 0, width, height)

        val insets = insets
        val chartX = insets.left + JBUI.scale(36)
        val chartY = insets.top + JBUI.scale(8)
        val chartW = width - chartX - insets.right - JBUI.scale(8)
        val chartH = height - chartY - insets.bottom - JBUI.scale(20)

        // Draw something even if size is too small
        if (chartW <= 0 || chartH <= 0) {
            g2.color = textColor
            g2.font = g2.font.deriveFont(Font.PLAIN, JBUI.scale(11).toFloat())
            g2.drawString("Chart area too small (${width}x${height})", JBUI.scale(8), JBUI.scale(20))
            g2.dispose()
            return
        }

        // Always draw grid and axes
        val chartArea = java.awt.Rectangle(chartX, chartY, chartW, chartH)
        drawGrid(g2, chartArea)
        drawAxes(g2, chartArea)

        val gpuPoints: List<Double>
        val memPoints: List<Double>
        synchronized(gpuBuffer) {
            gpuPoints = gpuBuffer.toList()
            memPoints = memBuffer.toList()
        }

        if (gpuPoints.isNotEmpty()) {
            drawAreaChart(g2, gpuPoints, chartArea)
            drawLine(g2, memPoints, chartArea, memLineColor)
            drawOverlayText(g2, gpuPoints.last(), chartX, chartY, chartW)
        } else {
            // Show waiting message
            g2.color = textColor
            g2.font = g2.font.deriveFont(Font.ITALIC, JBUI.scale(12).toFloat())
            val msg = "Waiting for GPU data..."
            val fm = g2.fontMetrics
            g2.drawString(msg, chartX + (chartW - fm.stringWidth(msg)) / 2, chartY + chartH / 2)
        }

        g2.dispose()
    }

    private fun drawGrid(g2: Graphics2D, area: java.awt.Rectangle) {
        val x = area.x
        val y = area.y
        val w = area.width
        val h = area.height
        g2.color = gridColor
        g2.stroke = BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(4f, 4f), 0f)
        for (i in 0..4) {
            val lineY = y + h - (h * i / 4)
            g2.drawLine(x, lineY, x + w, lineY)
        }
        for (i in 0..6) {
            val lineX = x + (w * i / 6)
            g2.drawLine(lineX, y, lineX, y + h)
        }
    }

    private fun drawAreaChart(g2: Graphics2D, points: List<Double>, area: java.awt.Rectangle) {
        val x = area.x
        val y = area.y
        val w = area.width
        val h = area.height
        val n = points.size
        val xStep = if (n > 1) w.toDouble() / (MAX_POINTS - 1) else 0.0
        val xOffset = if (n < MAX_POINTS) (MAX_POINTS - n) * xStep else 0.0

        val polyX = IntArray(n + 2)
        val polyY = IntArray(n + 2)

        for (i in points.indices) {
            polyX[i] = (x + xOffset + i * xStep).toInt()
            polyY[i] = (y + h - h * points[i]).toInt()
        }
        polyX[n] = (x + xOffset + (n - 1) * xStep).toInt()
        polyY[n] = y + h
        polyX[n + 1] = (x + xOffset).toInt()
        polyY[n + 1] = y + h

        // Area fill with gradient
        val avgUtil = points.average()
        val fillColor = utilizationColor(avgUtil, 80)
        g2.color = fillColor
        g2.fillPolygon(polyX, polyY, n + 2)

        // Line on top
        g2.color = utilizationColor(avgUtil, 220)
        g2.stroke = BasicStroke(JBUI.scale(2).toFloat())
        for (i in 0 until n - 1) {
            g2.drawLine(polyX[i], polyY[i], polyX[i + 1], polyY[i + 1])
        }
    }

    private fun drawLine(g2: Graphics2D, points: List<Double>, area: java.awt.Rectangle, color: Color) {
        val x = area.x
        val y = area.y
        val w = area.width
        val h = area.height
        if (points.size < 2) return
        val n = points.size
        val xStep = if (n > 1) w.toDouble() / (MAX_POINTS - 1) else 0.0
        val xOffset = if (n < MAX_POINTS) (MAX_POINTS - n) * xStep else 0.0

        g2.color = color
        g2.stroke = BasicStroke(JBUI.scale(1).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        for (i in 0 until n - 1) {
            val x1 = (x + xOffset + i * xStep).toInt()
            val y1 = (y + h - h * points[i]).toInt()
            val x2 = (x + xOffset + (i + 1) * xStep).toInt()
            val y2 = (y + h - h * points[i + 1]).toInt()
            g2.drawLine(x1, y1, x2, y2)
        }
    }

    private fun drawOverlayText(g2: Graphics2D, currentValue: Double, x: Int, y: Int, w: Int) {
        val pct = (currentValue * 100).toInt()
        g2.font = g2.font.deriveFont(Font.BOLD, JBUI.scale(14).toFloat())
        g2.color = textColor
        val text = "GPU $pct%"
        val fm = g2.fontMetrics
        g2.drawString(text, x + w - fm.stringWidth(text) - JBUI.scale(4), y + fm.height + JBUI.scale(2))
    }

    private fun drawAxes(g2: Graphics2D, area: java.awt.Rectangle) {
        val x = area.x
        val y = area.y
        val w = area.width
        val h = area.height
        g2.font = g2.font.deriveFont(Font.PLAIN, JBUI.scale(10).toFloat())
        g2.color = textColor
        val fm = g2.fontMetrics
        // Y-axis labels
        for (i in 0..4) {
            val label = "${i * 25}%"
            val labelY = y + h - (h * i / 4) + fm.ascent / 2
            g2.drawString(label, x - fm.stringWidth(label) - JBUI.scale(4), labelY)
        }
        // X-axis label
        g2.drawString("Last 60s", x + w - fm.stringWidth("Last 60s"), y + h + fm.height + JBUI.scale(2))
    }

    private fun utilizationColor(utilization: Double, alpha: Int): Color {
        val safeAlpha = alpha.coerceIn(0, 255)
        return when {
            utilization < 0.5 -> JBColor(
                Color(0x4C, 0xAF, 0x50, safeAlpha),
                Color(0x66, 0xBB, 0x6A, safeAlpha)
            )
            utilization <= 0.8 -> JBColor(
                Color(0xFF, 0xB3, 0x00, safeAlpha),
                Color(0xFF, 0xCA, 0x28, safeAlpha)
            )
            else -> JBColor(
                Color(0xF4, 0x43, 0x36, safeAlpha),
                Color(0xEF, 0x53, 0x50, safeAlpha)
            )
        }
    }

    override fun dispose() {
        synchronized(gpuBuffer) {
            gpuBuffer.clear()
            memBuffer.clear()
        }
    }

    companion object {
        // 120 points for 60s at 500ms resolution
        private const val MAX_POINTS = 120
    }
}
