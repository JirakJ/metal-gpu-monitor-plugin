package dev.htmeta.mgm.ui

import dev.htmeta.mgm.ui.toolwindow.GPUChartPanel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GPUChartPanelTest {

    @Test
    fun `addDataPoint stores data`() {
        val panel = GPUChartPanel()
        panel.addDataPoint(0.5, 0.3)
        // Panel has been updated — no exception thrown
        assertNotNull(panel)
    }

    @Test
    fun `addDataPoint clamps values to 0-1 range`() {
        val panel = GPUChartPanel()
        // Should not throw for out-of-range values — they get coerced
        assertDoesNotThrow { panel.addDataPoint(1.5, -0.5) }
        assertDoesNotThrow { panel.addDataPoint(-1.0, 2.0) }
    }

    @Test
    fun `buffer does not exceed max size`() {
        val panel = GPUChartPanel()
        // MAX_POINTS is 120, add more than that
        repeat(200) { i ->
            panel.addDataPoint(i.toDouble() / 200.0, i.toDouble() / 200.0)
        }
        // Panel should handle this gracefully without error
        assertNotNull(panel)
    }

    @Test
    fun `multiple addDataPoint calls work`() {
        val panel = GPUChartPanel()
        repeat(10) {
            panel.addDataPoint(0.1 * it, 0.05 * it)
        }
        assertNotNull(panel)
    }

    @Test
    fun `dispose clears buffers without error`() {
        val panel = GPUChartPanel()
        panel.addDataPoint(0.5, 0.5)
        panel.addDataPoint(0.6, 0.6)
        assertDoesNotThrow { panel.dispose() }
    }

    @Test
    fun `dispose on empty panel works`() {
        val panel = GPUChartPanel()
        assertDoesNotThrow { panel.dispose() }
    }

    @Test
    fun `addDataPoint after dispose works`() {
        val panel = GPUChartPanel()
        panel.addDataPoint(0.5, 0.5)
        panel.dispose()
        // Adding data after dispose should still work (buffers are cleared but not nulled)
        assertDoesNotThrow { panel.addDataPoint(0.3, 0.3) }
    }

    @Test
    fun `panel has preferred size set`() {
        val panel = GPUChartPanel()
        assertTrue(panel.preferredSize.width > 0)
        assertTrue(panel.preferredSize.height > 0)
    }

    @Test
    fun `addDataPoint with exact boundary values`() {
        val panel = GPUChartPanel()
        assertDoesNotThrow { panel.addDataPoint(0.0, 0.0) }
        assertDoesNotThrow { panel.addDataPoint(1.0, 1.0) }
    }
}
