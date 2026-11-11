package dev.htmeta.mgm.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.ui.JBColor
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import dev.htmeta.mgm.model.ProcessGPUUsage
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableRowSorter

class ProcessTablePanel : JPanel(BorderLayout()), Disposable {

    private val tableModel = ProcessTableModel()
    private val table = JBTable(tableModel)
    private val sorter = TableRowSorter(tableModel)
    init {
        border = JBUI.Borders.empty(4)
        preferredSize = java.awt.Dimension(JBUI.scale(400), JBUI.scale(150))
        minimumSize = java.awt.Dimension(JBUI.scale(200), JBUI.scale(80))

        table.rowSorter = sorter
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        table.rowHeight = JBUI.scale(22)
        table.setDefaultRenderer(Any::class.java, ProcessCellRenderer())

        val scrollPane = JScrollPane(table)
        add(scrollPane, BorderLayout.CENTER)
    }

    fun updateProcesses(processes: List<ProcessGPUUsage>) {
        tableModel.setData(processes)
    }

    override fun dispose() {
        // Nothing to release: the table holds no timers or listeners of its own.
    }

    private class ProcessTableModel : AbstractTableModel() {
        private var data: List<ProcessGPUUsage> = emptyList()

        fun setData(newData: List<ProcessGPUUsage>) {
            data = newData
            fireTableDataChanged()
        }

        override fun getRowCount(): Int = data.size
        override fun getColumnCount(): Int = COLUMNS.size
        override fun getColumnName(column: Int): String = COLUMNS[column]

        override fun getColumnClass(columnIndex: Int): Class<*> = when (columnIndex) {
            COL_PID -> java.lang.Integer::class.java
            COL_GPU_PCT -> java.lang.Double::class.java
            COL_GPU_MEM -> java.lang.Long::class.java
            else -> String::class.java
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val proc = data[rowIndex]
            return when (columnIndex) {
                COL_PID -> proc.pid
                COL_NAME -> proc.processName
                COL_GPU_PCT -> proc.gpuUtilization
                COL_GPU_MEM -> proc.gpuMemoryBytes
                COL_SOURCE -> if (proc.isIDEProcess) "IDE" else ""
                else -> ""
            }
        }

        fun getProcessAt(row: Int): ProcessGPUUsage? = data.getOrNull(row)

        companion object {
            private val COLUMNS = arrayOf("PID", "Process Name", "GPU %", "GPU Memory", "Source")
            const val COL_PID = 0
            const val COL_NAME = 1
            const val COL_GPU_PCT = 2
            const val COL_GPU_MEM = 3
            const val COL_SOURCE = 4
        }
    }

    private inner class ProcessCellRenderer : DefaultTableCellRenderer() {
        private val ideColor = JBColor(java.awt.Color(0xE3F2FD), java.awt.Color(0x1A3A5C))

        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            val modelRow = table.convertRowIndexToModel(row)
            val proc = tableModel.getProcessAt(modelRow)

            if (!isSelected && proc?.isIDEProcess == true) {
                comp.background = ideColor
            }

            // Format display values
            text = when (column) {
                ProcessTableModel.COL_GPU_PCT -> {
                    // macOS does not expose per-process GPU utilisation; 0 means "not available".
                    val pct = (value as? Double ?: 0.0) * 100
                    if (pct <= 0.0) "—" else String.format(java.util.Locale.ROOT, "%.1f%%", pct)
                }
                ProcessTableModel.COL_GPU_MEM -> {
                    val bytes = value as? Long ?: 0L
                    formatMemory(bytes)
                }
                else -> value?.toString() ?: ""
            }

            return comp
        }

        private fun formatMemory(bytes: Long): String {
            if (bytes <= 0) return "—"
            val mb = bytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format(java.util.Locale.ROOT, "%.1f GB", mb / 1024.0)
            } else {
                String.format(java.util.Locale.ROOT, "%.0f MB", mb)
            }
        }
    }

    companion object
}
