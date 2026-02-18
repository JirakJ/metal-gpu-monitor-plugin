package dev.htmeta.mgm.native

import com.intellij.openapi.diagnostic.thisLogger
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import dev.htmeta.mgm.model.GPUDeviceInfo
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.model.GPUVendor
import dev.htmeta.mgm.model.ProcessGPUUsage
import dev.htmeta.mgm.model.ThermalState
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * GPU metrics provider that reads real-time data from macOS IOKit framework via JNA.
 *
 * Queries IOAccelerator / AGXAccelerator services for GPU utilization, memory allocation,
 * frequency, and thermal state. IOKit property keys vary across hardware generations
 * (Intel, AMD, Apple Silicon), so this implementation uses a discovery-based approach:
 * it reads the full "PerformanceStatistics" dictionary and searches for known key patterns.
 *
 * Thread-safe: all JNA calls are serialized through a [ReentrantLock].
 */
class IOKitGPUProvider : GPUProvider {

    private val log = thisLogger()
    private val lock = ReentrantLock()
    private var initialized = false
    private var cachedDeviceInfo: GPUDeviceInfo? = null

    // ---------------------------------------------------------------------------
    // JNA interface: IOKit.framework
    // ---------------------------------------------------------------------------
    @Suppress("FunctionName")
    private interface IOKit : Library {
        /** Returns a matching dictionary for the given service name. Caller does NOT release. */
        fun IOServiceMatching(name: String): Pointer?

        /** Populates [existing] with an iterator of services matching [matching]. */
        fun IOServiceGetMatchingServices(mainPort: Int, matching: Pointer?, existing: IntByReference): Int

        /** Returns the next entry from an IOKit iterator, or 0 when exhausted. */
        fun IOIteratorNext(iterator: Int): Int

        /** Creates a CFMutableDictionary of all properties for a registry entry. */
        fun IORegistryEntryCreateCFProperties(
            entry: Int,
            properties: PointerByReference,
            allocator: Pointer?,
            options: Int,
        ): Int

        /** Looks up a single property by key on a registry entry. Caller must release the value. */
        fun IORegistryEntryCreateCFProperty(
            entry: Int,
            key: Pointer,
            allocator: Pointer?,
            options: Int,
        ): Pointer?

        /** Releases an IOKit object handle. */
        fun IOObjectRelease(obj: Int): Int

        /** Returns the class name of an IOKit object. */
        fun IOObjectGetClass(obj: Int, className: ByteArray): Int

        companion object {
            const val KERN_SUCCESS = 0
            const val MASTER_PORT = 0 // kIOMainPortDefault
        }
    }

    // ---------------------------------------------------------------------------
    // JNA interface: CoreFoundation.framework
    // ---------------------------------------------------------------------------
    @Suppress("FunctionName")
    private interface CF : Library {
        fun CFDictionaryGetValue(dict: Pointer, key: Pointer): Pointer?
        fun CFDictionaryGetCount(dict: Pointer): Int
        fun CFDictionaryGetKeysAndValues(dict: Pointer, keys: Array<Pointer?>?, values: Array<Pointer?>?)
        fun CFNumberGetValue(number: Pointer, theType: Int, valuePtr: Pointer): Boolean
        fun CFStringCreateWithCString(alloc: Pointer?, cStr: String, encoding: Int): Pointer?
        fun CFStringGetCString(str: Pointer, buffer: ByteArray, bufferSize: Int, encoding: Int): Boolean
        fun CFStringGetLength(str: Pointer): Int
        fun CFGetTypeID(cf: Pointer): Long
        fun CFStringGetTypeID(): Long
        fun CFNumberGetTypeID(): Long
        fun CFDictionaryGetTypeID(): Long
        fun CFBooleanGetValue(boolean: Pointer): Boolean
        fun CFRelease(cf: Pointer)

        companion object {
            const val kCFStringEncodingUTF8 = 0x08000100
            // CFNumber types
            const val kCFNumberSInt32Type = 3
            const val kCFNumberSInt64Type = 4
            const val kCFNumberFloat64Type = 6
        }
    }

    // ---------------------------------------------------------------------------
    // JNA interface: libproc (libSystem.B.dylib)
    // ---------------------------------------------------------------------------
    @Suppress("FunctionName")
    private interface LibProc : Library {
        /** Lists all active PIDs. Returns number of bytes written to [buffer]. */
        fun proc_listallpids(buffer: IntArray?, bufferSize: Int): Int

        /** Gets the process name for [pid]. Returns length of name on success. */
        fun proc_name(pid: Int, buffer: ByteArray, bufferSize: Int): Int
    }

    // ---------------------------------------------------------------------------
    // Lazy-loaded native libraries
    // ---------------------------------------------------------------------------
    private var ioKit: IOKit? = null
    private var cf: CF? = null
    private var libProc: LibProc? = null

    // ---------------------------------------------------------------------------
    // Known IOKit key patterns for GPU metrics (vary across hardware)
    // ---------------------------------------------------------------------------
    private companion object {
        /** Service class names to probe – ordered by preference. */
        val GPU_SERVICE_NAMES = listOf(
            "AGXAccelerator",          // Apple Silicon
            "AGXAcceleratorG13X",      // M1/M2
            "AGXAcceleratorG14X",      // M3
            "AGXAcceleratorG15X",      // M4
            "IOAccelerator",           // Intel / AMD
            "AppleIntelHD5000Graphics" // Older Intel iGPU
        )

        /** Keys under PerformanceStatistics that may carry utilization (0-100 or 0.0-1.0). */
        val UTILIZATION_KEYS = listOf(
            "Device Utilization %",
            "GPU Activity(%)",
            "GPU Core Utilization",
            "hardwareWaitTime",
            "GPU Busy",
            "gpuCoreUtilizationPercent",
        )

        /** Keys for allocated GPU memory bytes. */
        val MEMORY_ALLOC_KEYS = listOf(
            "Alloc system memory",
            "alloc_system",
            "allocSystemMemory",
            "vramUsedBytes",
            "In Use System Memory",
            "inUseSystemMemory",
        )

        /** Keys for total GPU memory bytes. */
        val MEMORY_TOTAL_KEYS = listOf(
            "VRAM,totalMB",
            "vramTotalBytes",
            "Total System Memory",
        )

        /** Keys for GPU core frequency (MHz). */
        val FREQUENCY_KEYS = listOf(
            "GPU Core Clock",
            "Core Clock(MHz)",
            "gpuCoreClockMHz",
        )

        /** Keys for power draw (Watts or milliwatts). */
        val POWER_KEYS = listOf(
            "GPU Power",
            "gpuPowerWatts",
            "Device Power(mW)",
        )

        val RENDER_UTIL_KEYS = listOf(
            "Renderer Utilization %",
            "rendererUtilization",
        )

        val COMPUTE_UTIL_KEYS = listOf(
            "Tiler Utilization %",
            "tilerUtilization",
            "Compute Utilization %",
        )
    }

    // =========================================================================
    // GPUProvider API
    // =========================================================================

    override fun initialize(): Boolean = lock.withLock {
        if (initialized) return true
        try {
            log.info("IOKitGPUProvider: loading native libraries...")
            ioKit = Native.load("IOKit", IOKit::class.java)
            log.info("IOKitGPUProvider: IOKit loaded")
            cf = Native.load("CoreFoundation", CF::class.java)
            log.info("IOKitGPUProvider: CoreFoundation loaded")
            libProc = Native.load("System.B", LibProc::class.java)
            log.info("IOKitGPUProvider: libproc loaded")

            // Verify we can reach at least one GPU service
            log.info("IOKitGPUProvider: searching for GPU service...")
            val found = findGPUServiceEntry()
            if (found != 0) {
                ioKit!!.IOObjectRelease(found)
                initialized = true
                log.info("IOKitGPUProvider initialized — GPU service located")
            } else {
                log.warn("No GPU accelerator service found via IOKit. Searched: $GPU_SERVICE_NAMES")
            }
        } catch (e: UnsatisfiedLinkError) {
            log.warn("Failed to load native libraries for IOKit GPU provider: ${e.message}", e)
        } catch (e: Exception) {
            log.error("Unexpected error during IOKitGPUProvider initialization: ${e.message}", e)
        }
        log.info("IOKitGPUProvider initialization result: $initialized")
        return initialized
    }

    override fun isAvailable(): Boolean = initialized

    override fun fetchSnapshot(): GPUSnapshot = lock.withLock {
        if (!initialized) return GPUSnapshot.UNAVAILABLE
        try {
            return readSnapshot()
        } catch (e: Exception) {
            log.warn("Error fetching GPU snapshot", e)
            return GPUSnapshot.UNAVAILABLE
        }
    }

    override fun fetchProcesses(): List<ProcessGPUUsage> = lock.withLock {
        if (!initialized) return emptyList()
        try {
            return readProcesses()
        } catch (e: Exception) {
            log.warn("Error fetching GPU processes", e)
            return emptyList()
        }
    }

    override fun deviceInfo(): GPUDeviceInfo {
        cachedDeviceInfo?.let { return it }
        return lock.withLock {
            cachedDeviceInfo?.let { return it }
            if (!initialized) return GPUDeviceInfo.UNKNOWN
            try {
                val info = readDeviceInfo()
                cachedDeviceInfo = info
                info
            } catch (e: Exception) {
                log.warn("Error reading device info", e)
                GPUDeviceInfo.UNKNOWN
            }
        }
    }

    override fun cleanup() = lock.withLock {
        initialized = false
        cachedDeviceInfo = null
        ioKit = null
        cf = null
        libProc = null
        log.info("IOKitGPUProvider cleaned up")
    }

    // =========================================================================
    // Internal: locate GPU service
    // =========================================================================

    /**
     * Iterates [GPU_SERVICE_NAMES] and returns the first matching IOService entry,
     * or `0` if none found. Caller is responsible for releasing the returned handle.
     */
    private fun findGPUServiceEntry(): Int {
        val kit = ioKit ?: return 0
        for (serviceName in GPU_SERVICE_NAMES) {
            val matchDict = kit.IOServiceMatching(serviceName) ?: continue
            val iterator = IntByReference()
            val kr = kit.IOServiceGetMatchingServices(IOKit.MASTER_PORT, matchDict, iterator)
            if (kr != IOKit.KERN_SUCCESS) continue
            val entry = kit.IOIteratorNext(iterator.value)
            kit.IOObjectRelease(iterator.value)
            if (entry != 0) return entry
        }
        return 0
    }

    // =========================================================================
    // Internal: read snapshot
    // =========================================================================

    private fun readSnapshot(): GPUSnapshot {
        val kit = ioKit ?: return GPUSnapshot.UNAVAILABLE
        val core = cf ?: return GPUSnapshot.UNAVAILABLE

        val entry = findGPUServiceEntry()
        if (entry == 0) return GPUSnapshot.UNAVAILABLE
        try {
            val propsRef = PointerByReference()
            val kr = kit.IORegistryEntryCreateCFProperties(entry, propsRef, null, 0)
            if (kr != IOKit.KERN_SUCCESS || propsRef.value == null) return GPUSnapshot.UNAVAILABLE

            val props = propsRef.value!!
            try {
                val perfStats = getDictionaryValue(props, "PerformanceStatistics")

                // Source dict: prefer PerformanceStatistics sub-dict, fall back to root properties
                val source = perfStats ?: props

                val utilization = findDoubleMetric(source, UTILIZATION_KEYS)
                val memAlloc = findLongMetric(source, MEMORY_ALLOC_KEYS)
                val memTotal = findLongMetric(source, MEMORY_TOTAL_KEYS)
                val frequency = findLongMetric(source, FREQUENCY_KEYS)?.toInt()
                val power = findDoubleMetric(source, POWER_KEYS)
                val renderUtil = findDoubleMetric(source, RENDER_UTIL_KEYS)
                val computeUtil = findDoubleMetric(source, COMPUTE_UTIL_KEYS)
                val thermal = readThermalState()

                // Normalize utilization: IOKit may report 0-100 or 0.0-1.0
                val gpuUtil = when {
                    utilization == null -> 0.0
                    utilization > 1.0 -> (utilization / 100.0).coerceIn(0.0, 1.0)
                    else -> utilization.coerceIn(0.0, 1.0)
                }

                return GPUSnapshot(
                    timestamp = System.currentTimeMillis(),
                    gpuUtilization = gpuUtil,
                    gpuFrequencyMHz = frequency,
                    memoryAllocatedBytes = memAlloc ?: 0L,
                    memoryTotalBytes = memTotal ?: deviceInfo().memoryTotalBytes,
                    thermalState = thermal,
                    gpuPowerWatts = normalizePower(power),
                    renderUtilization = renderUtil?.let { normalizePercent(it) },
                    computeUtilization = computeUtil?.let { normalizePercent(it) },
                )
            } finally {
                core.CFRelease(props)
            }
        } finally {
            kit.IOObjectRelease(entry)
        }
    }

    // =========================================================================
    // Internal: device info
    // =========================================================================

    private fun readDeviceInfo(): GPUDeviceInfo {
        val kit = ioKit ?: return GPUDeviceInfo.UNKNOWN
        val core = cf ?: return GPUDeviceInfo.UNKNOWN

        val entry = findGPUServiceEntry()
        if (entry == 0) return GPUDeviceInfo.UNKNOWN
        try {
            val propsRef = PointerByReference()
            val kr = kit.IORegistryEntryCreateCFProperties(entry, propsRef, null, 0)
            if (kr != IOKit.KERN_SUCCESS || propsRef.value == null) return GPUDeviceInfo.UNKNOWN

            val props = propsRef.value!!
            try {
                val name = getStringValue(props, "model")
                    ?: getClassName(entry)
                    ?: "Unknown GPU"

                val vendor = detectVendor(name)
                val memTotal = findLongMetric(props, MEMORY_TOTAL_KEYS) ?: estimateSystemMemory()
                val coreCount = getLongValue(props, "gpu-core-count")?.toInt() ?: 0
                val maxClock = getLongValue(props, "gpu-max-clock-mhz")?.toInt()

                val metalFeature = when (vendor) {
                    GPUVendor.APPLE -> "Metal 3"
                    GPUVendor.AMD -> "Metal 2"
                    GPUVendor.INTEL -> "Metal 1"
                    else -> null
                }

                return GPUDeviceInfo(
                    name = name,
                    vendor = vendor,
                    coreCount = coreCount,
                    maxClockMHz = maxClock,
                    memoryTotalBytes = memTotal,
                    metalFeatureSet = metalFeature,
                )
            } finally {
                core.CFRelease(props)
            }
        } finally {
            kit.IOObjectRelease(entry)
        }
    }

    private fun detectVendor(name: String): GPUVendor {
        val lower = name.lowercase()
        return when {
            "apple" in lower || "agx" in lower || "m1" in lower || "m2" in lower || "m3" in lower || "m4" in lower -> GPUVendor.APPLE
            "amd" in lower || "radeon" in lower -> GPUVendor.AMD
            "intel" in lower -> GPUVendor.INTEL
            else -> GPUVendor.UNKNOWN
        }
    }

    /**
     * Falls back to total physical memory as an approximation for unified-memory Apple Silicon.
     */
    private fun estimateSystemMemory(): Long {
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("sysctl", "-n", "hw.memsize"))
            val output = proc.inputStream.bufferedReader().readText().trim()
            proc.waitFor()
            output.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    // =========================================================================
    // Internal: process GPU usage
    // =========================================================================

    /**
     * Enumerates running processes and reports those likely using the GPU.
     *
     * True per-process GPU utilization is not directly exposed by macOS to user-space
     * without SPI. This implementation lists processes and tags the current IDE process;
     * a future version could parse `IOAccelerator` client lists for richer data.
     */
    private fun readProcesses(): List<ProcessGPUUsage> {
        val proc = libProc ?: return emptyList()
        val results = mutableListOf<ProcessGPUUsage>()

        // Get all PIDs
        val estimatedCount = proc.proc_listallpids(null, 0) / Int.SIZE_BYTES
        if (estimatedCount <= 0) return emptyList()
        val pidBuffer = IntArray(estimatedCount + 64)
        val bytesWritten = proc.proc_listallpids(pidBuffer, pidBuffer.size * Int.SIZE_BYTES)
        if (bytesWritten <= 0) return emptyList()
        val pidCount = bytesWritten / Int.SIZE_BYTES

        val myPid = ProcessHandle.current().pid().toInt()
        val nameBuffer = ByteArray(256)

        for (i in 0 until pidCount) {
            val pid = pidBuffer[i]
            if (pid <= 0) continue

            nameBuffer.fill(0)
            val nameLen = proc.proc_name(pid, nameBuffer, nameBuffer.size)
            if (nameLen <= 0) continue

            val name = String(nameBuffer, 0, nameLen)
            val isIDE = pid == myPid

            // Only include the IDE process in the fallback list — we cannot determine
            // actual per-process GPU usage without private SPI
            if (isIDE) {
                results.add(
                    ProcessGPUUsage(
                        pid = pid,
                        processName = name,
                        gpuUtilization = 0.0,
                        gpuMemoryBytes = 0L,
                        isIDEProcess = true,
                    )
                )
            }
        }
        return results
    }

    // =========================================================================
    // Internal: thermal state
    // =========================================================================

    private fun readThermalState(): ThermalState {
        val kit = ioKit ?: return ThermalState.NOMINAL
        val core = cf ?: return ThermalState.NOMINAL

        val matchDict = kit.IOServiceMatching("AppleSMC") ?: return ThermalState.NOMINAL
        val iterator = IntByReference()
        if (kit.IOServiceGetMatchingServices(IOKit.MASTER_PORT, matchDict, iterator) != IOKit.KERN_SUCCESS) {
            return ThermalState.NOMINAL
        }
        val entry = kit.IOIteratorNext(iterator.value)
        kit.IOObjectRelease(iterator.value)
        if (entry == 0) return ThermalState.NOMINAL

        try {
            val propsRef = PointerByReference()
            if (kit.IORegistryEntryCreateCFProperties(entry, propsRef, null, 0) != IOKit.KERN_SUCCESS) {
                return ThermalState.NOMINAL
            }
            val props = propsRef.value ?: return ThermalState.NOMINAL
            try {
                // Check for common thermal-level keys
                val level = getLongValue(props, "ThermalLevel")
                    ?: getLongValue(props, "thermal-level")
                    ?: return ThermalState.NOMINAL
                return ThermalState.fromNative(level.toInt())
            } finally {
                core.CFRelease(props)
            }
        } finally {
            kit.IOObjectRelease(entry)
        }
    }

    // =========================================================================
    // CFDictionary helpers
    // =========================================================================

    /** Searches [source] for the first matching key from [candidates] and returns its Double value. */
    private fun findDoubleMetric(source: Pointer, candidates: List<String>): Double? {
        for (key in candidates) {
            val value = getNumberValue(source, key)
            if (value != null) return value
        }
        return null
    }

    /** Searches [source] for the first matching key from [candidates] and returns its Long value. */
    private fun findLongMetric(source: Pointer, candidates: List<String>): Long? {
        for (key in candidates) {
            val value = getLongValue(source, key)
            if (value != null) return value
        }
        return null
    }

    /** Retrieves a nested CFDictionary from a parent dictionary. */
    private fun getDictionaryValue(dict: Pointer, key: String): Pointer? {
        val core = cf ?: return null
        val cfKey = core.CFStringCreateWithCString(null, key, CF.kCFStringEncodingUTF8) ?: return null
        try {
            val value = core.CFDictionaryGetValue(dict, cfKey) ?: return null
            return if (core.CFGetTypeID(value) == core.CFDictionaryGetTypeID()) value else null
        } finally {
            core.CFRelease(cfKey)
        }
    }

    /** Reads a CFNumber as Double from a CFDictionary. */
    private fun getNumberValue(dict: Pointer, key: String): Double? {
        val core = cf ?: return null
        val cfKey = core.CFStringCreateWithCString(null, key, CF.kCFStringEncodingUTF8) ?: return null
        try {
            val value = core.CFDictionaryGetValue(dict, cfKey) ?: return null
            if (core.CFGetTypeID(value) != core.CFNumberGetTypeID()) return null
            val buf = LongByReference()
            return if (core.CFNumberGetValue(value, CF.kCFNumberSInt64Type, buf.pointer)) {
                buf.value.toDouble()
            } else {
                null
            }
        } finally {
            core.CFRelease(cfKey)
        }
    }

    /** Reads a CFNumber as Long from a CFDictionary. */
    private fun getLongValue(dict: Pointer, key: String): Long? {
        val core = cf ?: return null
        val cfKey = core.CFStringCreateWithCString(null, key, CF.kCFStringEncodingUTF8) ?: return null
        try {
            val value = core.CFDictionaryGetValue(dict, cfKey) ?: return null
            if (core.CFGetTypeID(value) != core.CFNumberGetTypeID()) return null
            val buf = LongByReference()
            return if (core.CFNumberGetValue(value, CF.kCFNumberSInt64Type, buf.pointer)) {
                buf.value
            } else {
                null
            }
        } finally {
            core.CFRelease(cfKey)
        }
    }

    /** Reads a CFString value from a CFDictionary. */
    private fun getStringValue(dict: Pointer, key: String): String? {
        val core = cf ?: return null
        val cfKey = core.CFStringCreateWithCString(null, key, CF.kCFStringEncodingUTF8) ?: return null
        try {
            val value = core.CFDictionaryGetValue(dict, cfKey) ?: return null
            if (core.CFGetTypeID(value) != core.CFStringGetTypeID()) return null
            val buf = ByteArray(512)
            return if (core.CFStringGetCString(value, buf, buf.size, CF.kCFStringEncodingUTF8)) {
                Native.toString(buf, Charsets.UTF_8.name())
            } else {
                null
            }
        } finally {
            core.CFRelease(cfKey)
        }
    }

    /** Returns the IOKit class name for an object, or null. */
    private fun getClassName(entry: Int): String? {
        val kit = ioKit ?: return null
        val buf = ByteArray(128)
        return if (kit.IOObjectGetClass(entry, buf) == IOKit.KERN_SUCCESS) {
            Native.toString(buf, Charsets.UTF_8.name())
        } else {
            null
        }
    }

    // =========================================================================
    // Normalization helpers
    // =========================================================================

    /** Normalizes a percent value that may be 0-100 or 0.0-1.0 to 0.0-1.0. */
    private fun normalizePercent(value: Double): Double = when {
        value > 1.0 -> (value / 100.0).coerceIn(0.0, 1.0)
        else -> value.coerceIn(0.0, 1.0)
    }

    /** Normalizes power: if value > 1000 assume milliwatts and convert. */
    private fun normalizePower(value: Double?): Double? {
        if (value == null) return null
        return if (value > 1000.0) value / 1000.0 else value
    }
}
