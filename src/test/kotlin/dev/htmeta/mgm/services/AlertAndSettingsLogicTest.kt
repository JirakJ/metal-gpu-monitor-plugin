/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.htmeta.mgm.services

import dev.htmeta.mgm.feedback.FeedbackNotifier
import dev.htmeta.mgm.feedback.FeedbackState
import dev.htmeta.mgm.model.ThermalState
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.model.sample
import dev.htmeta.mgm.settings.GPUSettingsState
import dev.htmeta.mgm.settings.RefreshIntervals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class AlertAndSettingsLogicTest {

    private val settings = GPUSettingsState.State()
    private val now = 1_700_000_000_000L

    @Test
    fun `rules follow the configured thresholds and cooldown`() {
        val rules = GPUAlertService.rulesFor(settings.copy(gpuUtilizationThreshold = 0.7, alertCooldownSeconds = 30))
        assertEquals(listOf("gpu_high", "mem_high"), rules.map { it.id })
        assertEquals(0.7, rules[0].threshold, 1e-9)
        assertTrue(rules.all { it.cooldownSeconds == 30 })
    }

    @Test
    fun `high utilization fires the gpu rule`() {
        val due = GPUAlertService.dueRules(GPUSnapshot.sample(gpuUtilization = 0.95, memoryAllocatedBytes = 0), settings, emptyMap(), now)
        assertEquals(listOf("gpu_high"), due.map { it.id })
    }

    @Test
    fun `nothing fires below thresholds or when alerts are disabled`() {
        val calm = GPUSnapshot.sample(gpuUtilization = 0.1, memoryAllocatedBytes = 0)
        assertTrue(GPUAlertService.dueRules(calm, settings, emptyMap(), now).isEmpty())
        val hot = GPUSnapshot.sample(gpuUtilization = 0.99, thermalState = ThermalState.CRITICAL)
        assertTrue(GPUAlertService.dueRules(hot, settings.copy(alertsEnabled = false), emptyMap(), now).isEmpty())
    }

    @Test
    fun `serious or critical thermal state fires the thermal rule`() {
        val hot = GPUSnapshot.sample(gpuUtilization = 0.1, memoryAllocatedBytes = 0, thermalState = ThermalState.SERIOUS)
        assertEquals(listOf("thermal"), GPUAlertService.dueRules(hot, settings, emptyMap(), now).map { it.id })
        val fair = hot.copy(thermalState = ThermalState.FAIR)
        assertTrue(GPUAlertService.dueRules(fair, settings, emptyMap(), now).isEmpty())
    }

    @Test
    fun `cooldown suppresses repeated alerts until it expires`() {
        val snapshot = GPUSnapshot.sample(gpuUtilization = 0.95, memoryAllocatedBytes = 0)
        val last = mapOf("gpu_high" to now - 10_000)
        assertTrue(GPUAlertService.dueRules(snapshot, settings, last, now).isEmpty())
        val rule = GPUAlertService.rulesFor(settings).first()
        assertTrue(GPUAlertService.isInCooldown(rule, last, now))
        assertFalse(GPUAlertService.isInCooldown(rule, last, now + settings.alertCooldownSeconds * 1000L))
        assertFalse(GPUAlertService.isInCooldown(rule, emptyMap(), now))
    }

    @Test
    fun `refresh intervals round-trip and fall back to the default`() {
        assertEquals(listOf("50ms", "100ms", "250ms", "500ms", "1s"), RefreshIntervals.labels.toList())
        RefreshIntervals.labels.forEach { assertEquals(it, RefreshIntervals.labelFor(RefreshIntervals.msFor(it))) }
        assertEquals("100ms", RefreshIntervals.labelFor(1234))
        assertEquals(RefreshIntervals.DEFAULT_MS, RefreshIntervals.msFor("2 seconds"))
        assertEquals(RefreshIntervals.DEFAULT_MS, RefreshIntervals.msFor(null))
    }

    @Test
    fun `settings defaults are selectable in the UI and loadState replaces state`() {
        val state = GPUSettingsState()
        assertEquals(RefreshIntervals.DEFAULT_MS, state.state.refreshIntervalMs)
        assertTrue(RefreshIntervals.labelFor(state.state.refreshIntervalMs) in RefreshIntervals.labels)
        assertEquals(GPUSettingsState.ExportFormat.JSON, state.state.exportFormat)
        val loaded = GPUSettingsState.State(refreshIntervalMs = 500, exportFormat = GPUSettingsState.ExportFormat.CSV)
        state.loadState(loaded)
        assertSame(loaded, state.state)
    }

    @Test
    fun `feedback prompt waits a week and is shown once`() {
        val day = TimeUnit.DAYS.toMillis(1)
        val fresh = FeedbackState.State()
        assertFalse(FeedbackNotifier.shouldShowFeedback(fresh, now))
        assertEquals(now, fresh.firstActivation)
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now), now + 7 * day - 1))
        assertTrue(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now), now + 7 * day))
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now, feedbackShown = true), now + 30 * day))
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(now, dismissed = true), now + 30 * day))
    }

    @Test
    fun `notification groups used in code are registered`() {
        val xml = javaClass.getResource("/META-INF/plugin.xml")!!.readText()
        assertTrue(xml.contains("<notificationGroup id=\"${FeedbackNotifier.NOTIFICATION_GROUP_ID}\""))
        assertTrue(xml.contains("<notificationGroup id=\"GPU Monitor Alerts\""))
    }
}
