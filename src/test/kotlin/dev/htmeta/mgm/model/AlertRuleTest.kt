package dev.htmeta.mgm.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AlertRuleTest {

    @Test
    fun `isTriggered GPU_UTILIZATION above threshold`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.8)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.9)
        assertTrue(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered GPU_UTILIZATION below threshold`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.8)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.5)
        assertFalse(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered GPU_UTILIZATION at exact threshold`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.8)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.8)
        // value > threshold is false when equal
        assertFalse(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered MEMORY_UTILIZATION above threshold`() {
        val rule = AlertRule("test", AlertMetric.MEMORY_UTILIZATION, 0.5)
        // 80 / 100 = 0.8 > 0.5
        val snapshot = GPUSnapshot.sample(memoryAllocatedBytes = 80, memoryTotalBytes = 100)
        assertTrue(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered MEMORY_UTILIZATION below threshold`() {
        val rule = AlertRule("test", AlertMetric.MEMORY_UTILIZATION, 0.9)
        // 50 / 100 = 0.5 < 0.9
        val snapshot = GPUSnapshot.sample(memoryAllocatedBytes = 50, memoryTotalBytes = 100)
        assertFalse(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered THERMAL_STATE above threshold`() {
        val rule = AlertRule("test", AlertMetric.THERMAL_STATE, 0.5)
        // CRITICAL level = 3, CRITICAL.level = 3, so 3/3 = 1.0 > 0.5
        val snapshot = GPUSnapshot.sample(thermalState = ThermalState.CRITICAL)
        assertTrue(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered THERMAL_STATE below threshold`() {
        val rule = AlertRule("test", AlertMetric.THERMAL_STATE, 0.5)
        // NOMINAL level = 0, 0/3 = 0.0 < 0.5
        val snapshot = GPUSnapshot.sample(thermalState = ThermalState.NOMINAL)
        assertFalse(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered THERMAL_STATE SERIOUS`() {
        val rule = AlertRule("test", AlertMetric.THERMAL_STATE, 0.5)
        // SERIOUS level = 2, 2/3 ≈ 0.667 > 0.5
        val snapshot = GPUSnapshot.sample(thermalState = ThermalState.SERIOUS)
        assertTrue(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered when disabled returns false`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.1, enabled = false)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.99)
        assertFalse(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered with Comparison BELOW`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.5, comparison = Comparison.BELOW)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.3)
        assertTrue(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered with Comparison BELOW not triggered when above`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.5, comparison = Comparison.BELOW)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.8)
        assertFalse(rule.isTriggered(snapshot))
    }

    @Test
    fun `isTriggered with Comparison BELOW at exact threshold`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.5, comparison = Comparison.BELOW)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.5)
        // value < threshold is false when equal
        assertFalse(rule.isTriggered(snapshot))
    }

    @Test
    fun `default comparison is ABOVE`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.8)
        assertEquals(Comparison.ABOVE, rule.comparison)
    }

    @Test
    fun `default cooldownSeconds is 60`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.8)
        assertEquals(60, rule.cooldownSeconds)
    }

    @Test
    fun `default enabled is true`() {
        val rule = AlertRule("test", AlertMetric.GPU_UTILIZATION, 0.8)
        assertTrue(rule.enabled)
    }

    @Test
    fun `AlertMetric labels`() {
        assertEquals("GPU Utilization", AlertMetric.GPU_UTILIZATION.label)
        assertEquals("Memory Utilization", AlertMetric.MEMORY_UTILIZATION.label)
        assertEquals("Thermal State", AlertMetric.THERMAL_STATE.label)
    }
}
