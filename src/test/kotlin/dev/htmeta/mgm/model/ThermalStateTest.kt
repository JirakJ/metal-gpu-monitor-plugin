package dev.htmeta.mgm.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ThermalStateTest {

    @Test
    fun `fromNative 0 returns NOMINAL`() {
        assertEquals(ThermalState.NOMINAL, ThermalState.fromNative(0))
    }

    @Test
    fun `fromNative 1 returns FAIR`() {
        assertEquals(ThermalState.FAIR, ThermalState.fromNative(1))
    }

    @Test
    fun `fromNative 2 returns SERIOUS`() {
        assertEquals(ThermalState.SERIOUS, ThermalState.fromNative(2))
    }

    @Test
    fun `fromNative 3 returns CRITICAL`() {
        assertEquals(ThermalState.CRITICAL, ThermalState.fromNative(3))
    }

    @Test
    fun `fromNative negative value returns NOMINAL`() {
        assertEquals(ThermalState.NOMINAL, ThermalState.fromNative(-1))
    }

    @Test
    fun `fromNative value greater than 3 returns NOMINAL`() {
        assertEquals(ThermalState.NOMINAL, ThermalState.fromNative(4))
        assertEquals(ThermalState.NOMINAL, ThermalState.fromNative(100))
    }

    @Test
    fun `NOMINAL has correct level and label`() {
        assertEquals(0, ThermalState.NOMINAL.level)
        assertEquals("Nominal", ThermalState.NOMINAL.label)
    }

    @Test
    fun `FAIR has correct level and label`() {
        assertEquals(1, ThermalState.FAIR.level)
        assertEquals("Fair", ThermalState.FAIR.label)
    }

    @Test
    fun `SERIOUS has correct level and label`() {
        assertEquals(2, ThermalState.SERIOUS.level)
        assertEquals("Serious", ThermalState.SERIOUS.label)
    }

    @Test
    fun `CRITICAL has correct level and label`() {
        assertEquals(3, ThermalState.CRITICAL.level)
        assertEquals("Critical", ThermalState.CRITICAL.label)
    }

    @Test
    fun `all entries are present`() {
        assertEquals(4, ThermalState.entries.size)
    }
}
