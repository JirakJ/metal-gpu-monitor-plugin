/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.htmeta.mgm.settings

/** Refresh intervals offered in the tool window, as label ↔ milliseconds. */
object RefreshIntervals {
    const val DEFAULT_MS: Long = 100L

    private val OPTIONS: List<Pair<String, Long>> = listOf(
        "50ms" to 50L,
        "100ms" to 100L,
        "250ms" to 250L,
        "500ms" to 500L,
        "1s" to 1000L,
    )

    /** Labels in display order. */
    val labels: Array<String> get() = OPTIONS.map { it.first }.toTypedArray()

    /** Label for [ms]; unknown values show the default. */
    fun labelFor(ms: Long): String = OPTIONS.firstOrNull { it.second == ms }?.first ?: labelFor(DEFAULT_MS)

    /** Milliseconds for [label]; unknown labels map to the default. */
    fun msFor(label: String?): Long = OPTIONS.firstOrNull { it.first == label }?.second ?: DEFAULT_MS
}
