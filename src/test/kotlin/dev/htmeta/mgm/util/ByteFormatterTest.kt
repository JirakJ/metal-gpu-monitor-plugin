package dev.htmeta.mgm.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ByteFormatterTest {

    // --- format() ---

    @Test
    fun `format 0 bytes`() {
        assertEquals("0 B", ByteFormatter.format(0))
    }

    @Test
    fun `format bytes less than 1KB`() {
        assertEquals("512 B", ByteFormatter.format(512))
        assertEquals("1 B", ByteFormatter.format(1))
    }

    @Test
    fun `format KB range`() {
        val result1024 = ByteFormatter.format(1024)
        assertTrue(result1024.contains("1") && result1024.endsWith("KB"), "Expected ~1.0 KB but got: $result1024")
        val result1536 = ByteFormatter.format(1536)
        assertTrue(result1536.contains("1") && result1536.endsWith("KB"), "Expected ~1.5 KB but got: $result1536")
    }

    @Test
    fun `format MB range`() {
        val result = ByteFormatter.format(1_048_576)
        assertTrue(result.contains("1") && result.endsWith("MB"), "Expected ~1.0 MB but got: $result")
        val result5 = ByteFormatter.format(5_242_880)
        assertTrue(result5.contains("5") && result5.endsWith("MB"), "Expected ~5.0 MB but got: $result5")
    }

    @Test
    fun `format GB range`() {
        val result = ByteFormatter.format(1_073_741_824)
        assertTrue(result.contains("1") && result.endsWith("GB"), "Expected ~1.0 GB but got: $result")
        val result8 = ByteFormatter.format(8_589_934_592)
        assertTrue(result8.contains("8") && result8.endsWith("GB"), "Expected ~8.0 GB but got: $result8")
    }

    @Test
    fun `format TB range`() {
        val result = ByteFormatter.format(1_099_511_627_776)
        assertTrue(result.contains("1") && result.endsWith("TB"), "Expected ~1.0 TB but got: $result")
    }

    @Test
    fun `format negative returns NA`() {
        assertEquals("N/A", ByteFormatter.format(-1))
        assertEquals("N/A", ByteFormatter.format(-1000))
    }

    // --- formatShort() ---

    @Test
    fun `formatShort 0 bytes`() {
        assertEquals("0 B", ByteFormatter.formatShort(0))
    }

    @Test
    fun `formatShort bytes less than 1KB`() {
        assertEquals("512 B", ByteFormatter.formatShort(512))
    }

    @Test
    fun `formatShort KB range`() {
        val result = ByteFormatter.formatShort(1024)
        assertTrue(result.contains("1") && result.endsWith("KB"), "Expected ~1 KB but got: $result")
        val result1536 = ByteFormatter.formatShort(1536)
        assertTrue(result1536.contains("2") && result1536.endsWith("KB"), "Expected ~2 KB but got: $result1536")
    }

    @Test
    fun `formatShort MB range`() {
        val result = ByteFormatter.formatShort(1_048_576)
        assertTrue(result.contains("1") && result.endsWith("MB"), "Expected ~1 MB but got: $result")
        val result5 = ByteFormatter.formatShort(5_242_880)
        assertTrue(result5.contains("5") && result5.endsWith("MB"), "Expected ~5 MB but got: $result5")
    }

    @Test
    fun `formatShort GB range`() {
        val result = ByteFormatter.formatShort(1_073_741_824)
        assertTrue(result.contains("1") && result.endsWith("GB"), "Expected ~1.0 GB but got: $result")
        val result8 = ByteFormatter.formatShort(8_589_934_592)
        assertTrue(result8.contains("8") && result8.endsWith("GB"), "Expected ~8.0 GB but got: $result8")
    }

    @Test
    fun `formatShort negative returns NA`() {
        assertEquals("N/A", ByteFormatter.formatShort(-1))
        assertEquals("N/A", ByteFormatter.formatShort(-100))
    }

    @Test
    fun `format exactly 1024 bytes`() {
        val result = ByteFormatter.format(1024)
        assertTrue(result.contains("1") && result.endsWith("KB"), "Expected ~1.0 KB but got: $result")
    }

    @Test
    fun `formatShort exactly 1024 bytes`() {
        val result = ByteFormatter.formatShort(1024)
        assertTrue(result.contains("1") && result.endsWith("KB"), "Expected ~1 KB but got: $result")
    }
}
