/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.htmeta.mgm.actions

import dev.htmeta.mgm.model.GPUSnapshot

/** Renders GPU snapshots as JSON or CSV. Pure functions — the action only picks the file. */
object GPUDataExporter {

    val CSV_COLUMNS: List<String> = listOf(
        "timestamp", "gpuUtilization", "gpuFrequencyMHz", "memoryAllocatedBytes", "memoryTotalBytes",
        "memoryUtilization", "thermalState", "gpuPowerWatts", "renderUtilization", "computeUtilization"
    )

    /** JSON document with the export time, the current snapshot and the history (oldest first). */
    fun toJson(current: GPUSnapshot, history: List<GPUSnapshot>, exportTime: String): String = buildString {
        appendLine("{")
        appendLine("  \"exportTime\": \"${exportTime.replace("\\", "\\\\").replace("\"", "\\\"")}\",")
        appendLine("  \"current\": ${snapshotToJson(current)},")
        appendLine("  \"history\": [")
        history.forEachIndexed { index, snapshot ->
            val comma = if (index < history.size - 1) "," else ""
            appendLine("    ${snapshotToJson(snapshot)}$comma")
        }
        appendLine("  ]")
        appendLine("}")
    }

    /** Single snapshot as a JSON object; missing or non-finite numbers become `null`. */
    fun snapshotToJson(s: GPUSnapshot): String = buildString {
        append("{")
        append("\"timestamp\":${s.timestamp},")
        append("\"gpuUtilization\":${num(s.gpuUtilization)},")
        append("\"gpuFrequencyMHz\":${s.gpuFrequencyMHz ?: "null"},")
        append("\"memoryAllocatedBytes\":${s.memoryAllocatedBytes},")
        append("\"memoryTotalBytes\":${s.memoryTotalBytes},")
        append("\"memoryUtilization\":${num(s.memoryUtilization)},")
        append("\"thermalState\":\"${s.thermalState.name}\",")
        append("\"gpuPowerWatts\":${num(s.gpuPowerWatts)},")
        append("\"renderUtilization\":${num(s.renderUtilization)},")
        append("\"computeUtilization\":${num(s.computeUtilization)}")
        append("}")
    }

    /** CSV with a header row and one row per snapshot: history first, then the current snapshot. */
    fun toCsv(current: GPUSnapshot, history: List<GPUSnapshot>): String {
        val rows = (history + current).joinToString("\n") { s ->
            listOf(
                s.timestamp.toString(),
                s.gpuUtilization.toString(),
                s.gpuFrequencyMHz?.toString().orEmpty(),
                s.memoryAllocatedBytes.toString(),
                s.memoryTotalBytes.toString(),
                s.memoryUtilization.toString(),
                s.thermalState.name,
                s.gpuPowerWatts?.toString().orEmpty(),
                s.renderUtilization?.toString().orEmpty(),
                s.computeUtilization?.toString().orEmpty(),
            ).joinToString(",")
        }
        return CSV_COLUMNS.joinToString(",") + "\n" + rows + "\n"
    }

    private fun num(value: Double?): String = if (value == null || !value.isFinite()) "null" else value.toString()
}
