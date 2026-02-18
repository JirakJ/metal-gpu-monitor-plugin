package dev.htmeta.mgm.services

import dev.htmeta.mgm.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GPUAlertServiceTest {

    private val service = GPUAlertService()

    @Test
    fun `formatAlertMessage GPU utilization`() {
        val rule = AlertRule("gpu_high", AlertMetric.GPU_UTILIZATION, 0.8)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.95)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU utilization is at 95% (threshold: 80%)", message)
    }

    @Test
    fun `formatAlertMessage GPU utilization at zero`() {
        val rule = AlertRule("gpu_high", AlertMetric.GPU_UTILIZATION, 0.5)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.0)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU utilization is at 0% (threshold: 50%)", message)
    }

    @Test
    fun `formatAlertMessage memory utilization`() {
        val rule = AlertRule("mem_high", AlertMetric.MEMORY_UTILIZATION, 0.9)
        // 80/100 = 0.8 = 80%
        val snapshot = GPUSnapshot.sample(memoryAllocatedBytes = 80, memoryTotalBytes = 100)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU memory usage is at 80% (threshold: 90%)", message)
    }

    @Test
    fun `formatAlertMessage memory utilization zero total`() {
        val rule = AlertRule("mem_high", AlertMetric.MEMORY_UTILIZATION, 0.9)
        val snapshot = GPUSnapshot.sample(memoryAllocatedBytes = 0, memoryTotalBytes = 0)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU memory usage is at 0% (threshold: 90%)", message)
    }

    @Test
    fun `formatAlertMessage thermal state NOMINAL`() {
        val rule = AlertRule("thermal", AlertMetric.THERMAL_STATE, 0.5)
        val snapshot = GPUSnapshot.sample(thermalState = ThermalState.NOMINAL)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU thermal state: Nominal", message)
    }

    @Test
    fun `formatAlertMessage thermal state CRITICAL`() {
        val rule = AlertRule("thermal", AlertMetric.THERMAL_STATE, 0.5)
        val snapshot = GPUSnapshot.sample(thermalState = ThermalState.CRITICAL)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU thermal state: Critical", message)
    }

    @Test
    fun `formatAlertMessage thermal state SERIOUS`() {
        val rule = AlertRule("thermal", AlertMetric.THERMAL_STATE, 0.5)
        val snapshot = GPUSnapshot.sample(thermalState = ThermalState.SERIOUS)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU thermal state: Serious", message)
    }

    @Test
    fun `formatAlertMessage thermal state FAIR`() {
        val rule = AlertRule("thermal", AlertMetric.THERMAL_STATE, 0.5)
        val snapshot = GPUSnapshot.sample(thermalState = ThermalState.FAIR)
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU thermal state: Fair", message)
    }

    // Integration: AlertRule.isTriggered + formatAlertMessage

    @Test
    fun `integration - triggered rule produces correct message`() {
        val rule = AlertRule("gpu_high", AlertMetric.GPU_UTILIZATION, 0.8)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.92)
        assertTrue(rule.isTriggered(snapshot))
        val message = service.formatAlertMessage(rule, snapshot)
        assertTrue(message.contains("92%"))
        assertTrue(message.contains("80%"))
    }

    @Test
    fun `integration - non-triggered rule still formats correctly`() {
        val rule = AlertRule("gpu_low", AlertMetric.GPU_UTILIZATION, 0.8)
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.5)
        assertFalse(rule.isTriggered(snapshot))
        val message = service.formatAlertMessage(rule, snapshot)
        assertEquals("GPU utilization is at 50% (threshold: 80%)", message)
    }

    @Test
    fun `alertLog starts empty`() {
        val fresh = GPUAlertService()
        assertTrue(fresh.alertLog.isEmpty())
    }

    @Test
    fun `clearAlertLog clears the log`() {
        val fresh = GPUAlertService()
        fresh.clearAlertLog()
        assertTrue(fresh.alertLog.isEmpty())
    }

    @Test
    fun `dispose does not throw`() {
        val fresh = GPUAlertService()
        assertDoesNotThrow { fresh.dispose() }
    }
}
