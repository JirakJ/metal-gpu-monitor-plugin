package dev.htmeta.mgm.util

object ByteFormatter {
    fun format(bytes: Long): String {
        if (bytes < 0) return "N/A"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024.0
            unitIndex++
        }
        return if (unitIndex == 0) "$bytes B"
        else String.format("%.1f %s", value, units[unitIndex])
    }

    fun formatShort(bytes: Long): String {
        if (bytes < 0) return "N/A"
        return when {
            bytes >= 1_073_741_824L -> String.format("%.1f GB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576L -> String.format("%.0f MB", bytes / 1_048_576.0)
            bytes >= 1024L -> String.format("%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
