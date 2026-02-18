package dev.htmeta.mgm.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NativeBridgeResultTest {

    @Test
    fun `Success getOrNull returns value`() {
        val result: NativeBridgeResult<String> = NativeBridgeResult.Success("hello")
        assertEquals("hello", result.getOrNull())
    }

    @Test
    fun `Error getOrNull returns null`() {
        val result: NativeBridgeResult<String> = NativeBridgeResult.Error("fail")
        assertNull(result.getOrNull())
    }

    @Test
    fun `Unavailable getOrNull returns null`() {
        val result: NativeBridgeResult<String> = NativeBridgeResult.Unavailable
        assertNull(result.getOrNull())
    }

    @Test
    fun `Success getOrDefault returns value`() {
        val result: NativeBridgeResult<String> = NativeBridgeResult.Success("hello")
        assertEquals("hello", result.getOrDefault("default"))
    }

    @Test
    fun `Error getOrDefault returns default`() {
        val result: NativeBridgeResult<String> = NativeBridgeResult.Error("fail")
        assertEquals("default", result.getOrDefault("default"))
    }

    @Test
    fun `Unavailable getOrDefault returns default`() {
        val result: NativeBridgeResult<String> = NativeBridgeResult.Unavailable
        assertEquals("default", result.getOrDefault("default"))
    }

    @Test
    fun `map on Success transforms value`() {
        val result: NativeBridgeResult<Int> = NativeBridgeResult.Success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `map on Error passes through`() {
        val result: NativeBridgeResult<Int> = NativeBridgeResult.Error("fail", RuntimeException("cause"))
        val mapped = result.map { it * 2 }
        assertNull(mapped.getOrNull())
        assertTrue(mapped is NativeBridgeResult.Error)
        val error = mapped as NativeBridgeResult.Error
        assertEquals("fail", error.message)
        assertNotNull(error.cause)
    }

    @Test
    fun `map on Unavailable passes through`() {
        val result: NativeBridgeResult<Int> = NativeBridgeResult.Unavailable
        val mapped = result.map { it * 2 }
        assertNull(mapped.getOrNull())
        assertTrue(mapped is NativeBridgeResult.Unavailable)
    }

    @Test
    fun `Error with cause`() {
        val cause = RuntimeException("root")
        val result = NativeBridgeResult.Error("msg", cause)
        assertEquals("msg", result.message)
        assertEquals(cause, result.cause)
    }

    @Test
    fun `Error without cause defaults to null`() {
        val result = NativeBridgeResult.Error("msg")
        assertNull(result.cause)
    }

    @Test
    fun `Success data class equality`() {
        val a = NativeBridgeResult.Success(42)
        val b = NativeBridgeResult.Success(42)
        assertEquals(a, b)
    }

    @Test
    fun `map chain works correctly`() {
        val result: NativeBridgeResult<Int> = NativeBridgeResult.Success(5)
        val mapped = result.map { it + 1 }.map { it.toString() }
        assertEquals("6", mapped.getOrNull())
    }
}
