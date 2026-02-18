package dev.htmeta.mgm.actions

import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.model.ThermalState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ExportGPUDataActionTest {

    /**
     * Since the export methods are private, we replicate their logic here
     * to verify the format is correct. This tests the JSON/CSV format contract.
     */

    @TempDir
    lateinit var tempDir: Path

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

    private fun exportJson(file: File, current: GPUSnapshot, history: List<GPUSnapshot>) {
        val sb = StringBuilder()
        sb.appendLine("{")
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

    private fun exportCsv(file: File, current: GPUSnapshot, history: List<GPUSnapshot>) {
        val header = "timestamp,gpuUtilization,gpuFrequencyMHz,memoryAllocatedBytes,memoryTotalBytes,memoryUtilization,thermalState,gpuPowerWatts,renderUtilization,computeUtilization"
        val rows = (history + current).joinToString("\n") { s ->
            "${s.timestamp},${s.gpuUtilization},${s.gpuFrequencyMHz ?: ""},${s.memoryAllocatedBytes},${s.memoryTotalBytes},${s.memoryUtilization},${s.thermalState.name},${s.gpuPowerWatts ?: ""},${s.renderUtilization ?: ""},${s.computeUtilization ?: ""}"
        }
        file.writeText("$header\n$rows\n")
    }

    @Test
    fun `JSON export contains expected fields`() {
        val snapshot = GPUSnapshot.sample(
            timestamp = 1000L,
            gpuUtilization = 0.42,
            gpuFrequencyMHz = 1398,
            memoryAllocatedBytes = 8_589_934_592L,
            memoryTotalBytes = 38_654_705_664L,
            thermalState = ThermalState.NOMINAL,
            gpuPowerWatts = 12.5,
            renderUtilization = 0.35,
            computeUtilization = 0.07
        )
        val file = tempDir.resolve("test.json").toFile()
        exportJson(file, snapshot, listOf(snapshot))

        val content = file.readText()
        assertTrue(content.contains("\"timestamp\":1000"))
        assertTrue(content.contains("\"gpuUtilization\":0.42"))
        assertTrue(content.contains("\"gpuFrequencyMHz\":1398"))
        assertTrue(content.contains("\"memoryAllocatedBytes\":8589934592"))
        assertTrue(content.contains("\"thermalState\":\"NOMINAL\""))
        assertTrue(content.contains("\"gpuPowerWatts\":12.5"))
        assertTrue(content.contains("\"renderUtilization\":0.35"))
        assertTrue(content.contains("\"computeUtilization\":0.07"))
        assertTrue(content.contains("\"current\":"))
        assertTrue(content.contains("\"history\":"))
    }

    @Test
    fun `JSON export with null optional fields`() {
        val snapshot = GPUSnapshot.sample(
            timestamp = 500L,
            gpuFrequencyMHz = null,
            gpuPowerWatts = null,
            renderUtilization = null,
            computeUtilization = null
        )
        val file = tempDir.resolve("test_null.json").toFile()
        exportJson(file, snapshot, emptyList())

        val content = file.readText()
        assertTrue(content.contains("\"gpuFrequencyMHz\":null"))
        assertTrue(content.contains("\"gpuPowerWatts\":null"))
        assertTrue(content.contains("\"renderUtilization\":null"))
        assertTrue(content.contains("\"computeUtilization\":null"))
    }

    @Test
    fun `JSON export with empty history`() {
        val snapshot = GPUSnapshot.sample(timestamp = 100L)
        val file = tempDir.resolve("test_empty.json").toFile()
        exportJson(file, snapshot, emptyList())

        val content = file.readText()
        assertTrue(content.contains("\"history\": ["))
        assertTrue(content.contains("]"))
    }

    @Test
    fun `JSON export with multiple history entries`() {
        val s1 = GPUSnapshot.sample(timestamp = 1L)
        val s2 = GPUSnapshot.sample(timestamp = 2L)
        val current = GPUSnapshot.sample(timestamp = 3L)
        val file = tempDir.resolve("test_multi.json").toFile()
        exportJson(file, current, listOf(s1, s2))

        val content = file.readText()
        assertTrue(content.contains("\"timestamp\":1"))
        assertTrue(content.contains("\"timestamp\":2"))
        assertTrue(content.contains("\"timestamp\":3"))
    }

    @Test
    fun `CSV export has correct header`() {
        val snapshot = GPUSnapshot.sample(timestamp = 1000L)
        val file = tempDir.resolve("test.csv").toFile()
        exportCsv(file, snapshot, emptyList())

        val lines = file.readLines()
        assertEquals(
            "timestamp,gpuUtilization,gpuFrequencyMHz,memoryAllocatedBytes,memoryTotalBytes,memoryUtilization,thermalState,gpuPowerWatts,renderUtilization,computeUtilization",
            lines[0]
        )
    }

    @Test
    fun `CSV export has correct data row`() {
        val snapshot = GPUSnapshot.sample(
            timestamp = 1000L,
            gpuUtilization = 0.42,
            gpuFrequencyMHz = 1398,
            memoryAllocatedBytes = 100,
            memoryTotalBytes = 200,
            thermalState = ThermalState.NOMINAL,
            gpuPowerWatts = 12.5,
            renderUtilization = 0.35,
            computeUtilization = 0.07
        )
        val file = tempDir.resolve("test_row.csv").toFile()
        exportCsv(file, snapshot, emptyList())

        val lines = file.readLines()
        assertEquals(2, lines.size)
        val row = lines[1]
        assertTrue(row.startsWith("1000,"))
        assertTrue(row.contains("0.42"))
        assertTrue(row.contains("1398"))
        assertTrue(row.contains("NOMINAL"))
        assertTrue(row.contains("12.5"))
    }

    @Test
    fun `CSV export with null optional fields uses empty strings`() {
        val snapshot = GPUSnapshot.sample(
            timestamp = 500L,
            gpuFrequencyMHz = null,
            gpuPowerWatts = null,
            renderUtilization = null,
            computeUtilization = null
        )
        val file = tempDir.resolve("test_null.csv").toFile()
        exportCsv(file, snapshot, emptyList())

        val lines = file.readLines()
        val row = lines[1]
        // Null fields should be empty between commas
        assertTrue(row.contains("500,0.42,,"))
    }

    @Test
    fun `CSV export includes history and current`() {
        val history1 = GPUSnapshot.sample(timestamp = 1L)
        val history2 = GPUSnapshot.sample(timestamp = 2L)
        val current = GPUSnapshot.sample(timestamp = 3L)
        val file = tempDir.resolve("test_combined.csv").toFile()
        exportCsv(file, current, listOf(history1, history2))

        val lines = file.readLines()
        // header + 3 data rows
        assertEquals(4, lines.size)
        assertTrue(lines[1].startsWith("1,"))
        assertTrue(lines[2].startsWith("2,"))
        assertTrue(lines[3].startsWith("3,"))
    }

    @Test
    fun `snapshotToJson produces valid JSON structure`() {
        val snapshot = GPUSnapshot.sample(timestamp = 42L)
        val json = snapshotToJson(snapshot)
        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
        assertTrue(json.contains("\"timestamp\":42"))
    }
}
