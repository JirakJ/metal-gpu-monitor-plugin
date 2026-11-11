/*
 * Copyright 2026 Jakub Jirák
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.htmeta.mgm.native

import dev.htmeta.mgm.model.GPUVendor
import dev.htmeta.mgm.model.ProcessGPUUsage

/**
 * Platform-independent interpretation of the raw values the IOKit provider reads.
 *
 * Kept free of JNA so it is unit-tested on every OS; [IOKitGPUProvider] only does the
 * native calls and delegates the decisions here.
 */
internal object GPUMetrics {

    private const val PERCENT = 100.0
    private const val MILLIWATT_THRESHOLD = 1000.0
    private const val BYTES_PER_MB = 1024L * 1024L
    private const val SHORT_PATTERN_LENGTH = 4

    private const val WINDOW_SERVER = "WindowServer"

    /** Normalizes a percentage reported as 0–100 or 0.0–1.0 to 0.0–1.0. */
    fun normalizePercent(value: Double): Double =
        if (value > 1.0) (value / PERCENT).coerceIn(0.0, 1.0) else value.coerceIn(0.0, 1.0)

    /** Normalizes power: values above 1000 are milliwatts and are converted to watts. */
    fun normalizePower(value: Double?): Double? =
        value?.let { if (it > MILLIWATT_THRESHOLD) it / MILLIWATT_THRESHOLD else it }

    /** Converts a memory metric to bytes; registry keys ending in `MB` are megabytes. */
    fun memoryBytes(key: String, value: Long): Long =
        if (key.endsWith("MB")) value * BYTES_PER_MB else value

    /** Parses the output of `sysctl -n hw.memsize`; 0 when it is not a number. */
    fun parseMemSize(output: String): Long = output.trim().toLongOrNull() ?: 0L

    /** Vendor from the GPU model / IOKit class name. */
    fun detectVendor(name: String): GPUVendor {
        val lower = name.lowercase()
        return when {
            "apple" in lower || "agx" in lower || APPLE_CHIP.containsMatchIn(lower) -> GPUVendor.APPLE
            "amd" in lower || "radeon" in lower -> GPUVendor.AMD
            "intel" in lower -> GPUVendor.INTEL
            else -> GPUVendor.UNKNOWN
        }
    }

    /** Highest Metal family supported by GPUs of [vendor] that run current macOS releases. */
    fun metalFeatureSet(vendor: GPUVendor): String? = when (vendor) {
        GPUVendor.APPLE -> "Metal 3"
        GPUVendor.AMD -> "Metal 2"
        GPUVendor.INTEL -> "Metal 1"
        else -> null
    }

    /**
     * Whether a process name suggests GPU usage. Short patterns (≤ 4 chars, e.g. `arc`, `tv`, `mtl`)
     * must match a whole word so that `search` or `archive` are not reported as GPU processes.
     */
    fun isLikelyGpuProcess(name: String): Boolean {
        val lower = name.lowercase()
        val words = lower.split(NON_ALNUM).filter { it.isNotEmpty() }.toSet()
        return GPU_PROCESS_PATTERNS.any { pattern ->
            if (pattern.length <= SHORT_PATTERN_LENGTH) pattern in words else lower.contains(pattern)
        }
    }

    /**
     * Picks the processes to show from all `(pid, name)` pairs: the IDE itself, WindowServer and
     * likely GPU users, IDE first, then WindowServer, then in enumeration order.
     * macOS exposes no per-process GPU utilisation, so usage values are 0.
     */
    fun selectGpuProcesses(processes: List<Pair<Int, String>>, idePid: Int): List<ProcessGPUUsage> =
        processes
            .filter { (pid, name) -> pid > 0 && name.isNotEmpty() }
            .filter { (pid, name) ->
                pid == idePid || name.equals(WINDOW_SERVER, ignoreCase = true) || isLikelyGpuProcess(name)
            }
            .map { (pid, name) -> ProcessGPUUsage(pid, name, 0.0, 0L, isIDEProcess = pid == idePid) }
            .sortedWith(
                compareByDescending<ProcessGPUUsage> { it.isIDEProcess }
                    .thenByDescending { it.processName.equals(WINDOW_SERVER, ignoreCase = true) }
            )

    private val APPLE_CHIP = Regex("""\bm[1-9]\b""")
    private val NON_ALNUM = Regex("[^a-z0-9]+")

    val GPU_PROCESS_PATTERNS = listOf(
        // System GPU processes
        "windowserver", "gpuswitch", "mtlcompilerservice", "metalcompilerservice",
        "corebrightness", "skylight", "quartz", "coreanimation",
        // Browsers & web
        "chrome", "firefox", "safari", "webkit", "electron", "gpu process", "gpu-process",
        "chromium", "brave", "edge", "opera", "vivaldi", "arc",
        // JetBrains IDEs
        "idea", "webstorm", "pycharm", "clion", "rider", "goland", "rubymine", "phpstorm",
        "appcode", "datagrip", "fleet",
        // Apple apps
        "xcode", "simulator", "instruments", "finder", "dock", "systemuiserver",
        // Creative/3D
        "blender", "unity", "unreal", "maya", "cinema4d", "houdini", "zbrush",
        "premiere", "aftereffects", "photoshop", "illustrator", "lightroom",
        "figma", "sketch", "affinity", "davinci", "finalcut", "motion", "compressor",
        // Media
        "vlc", "iina", "quicktime", "music", "tv", "photos", "preview",
        // Games
        "steam", "game", "mtl"
    )
}
