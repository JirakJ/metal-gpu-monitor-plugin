package dev.htmeta.mgm.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import dev.htmeta.mgm.model.GPUDeviceInfo
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.model.ProcessGPUUsage
import dev.htmeta.mgm.native.GPUProvider
import dev.htmeta.mgm.native.GPUProviderFactory
import dev.htmeta.mgm.settings.GPUSettingsState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Service(Service.Level.APP)
class GPUMonitoringService : Disposable {
    private val LOG = thisLogger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _snapshots = MutableSharedFlow<GPUSnapshot>(replay = 1)
    val snapshots: SharedFlow<GPUSnapshot> = _snapshots.asSharedFlow()
    
    private val _processes = MutableSharedFlow<List<ProcessGPUUsage>>(replay = 1)
    val processes: SharedFlow<List<ProcessGPUUsage>> = _processes.asSharedFlow()
    
    private var monitoringJob: Job? = null
    private var provider: GPUProvider? = null
    private var _isMonitoring = false
    val isMonitoring: Boolean get() = _isMonitoring
    
    fun startMonitoring() {
        if (_isMonitoring) return

        val gpuProvider = provider ?: GPUProviderFactory().createProvider().also { provider = it }
        if (!gpuProvider.isAvailable()) {
            LOG.warn("No GPU provider available - monitoring disabled")
            // Emit unavailable snapshot so UI shows something
            scope.launch {
                _snapshots.emit(GPUSnapshot.UNAVAILABLE)
                _processes.emit(emptyList())
            }
            return
        }
        
        _isMonitoring = true
        monitoringJob = scope.launch {
            LOG.info("GPU monitoring started with interval ${GPUSettingsState.getInstance().state.refreshIntervalMs}ms")
            var loopCount = 0
            while (isActive) {
                try {
                    val snapshot = withContext(Dispatchers.IO) { gpuProvider.fetchSnapshot() }
                    _snapshots.emit(snapshot)

                    val procs = withContext(Dispatchers.IO) { gpuProvider.fetchProcesses() }
                    _processes.emit(procs)

                    loopCount++
                    LOG.debug { "monitoring loop #$loopCount GPU=${(snapshot.gpuUtilization * 100).toInt()}% processes=${procs.size}" }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    LOG.warn("GPU monitoring error", e)
                    _snapshots.emit(GPUSnapshot.UNAVAILABLE)
                }
                delay(GPUSettingsState.getInstance().state.refreshIntervalMs)
            }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring = false
        LOG.info("GPU monitoring stopped")
    }
    
    fun toggleMonitoring() {
        if (_isMonitoring) stopMonitoring() else startMonitoring()
    }

    fun restartMonitoring() {
        if (_isMonitoring) {
            LOG.info("Restarting monitoring with new interval: ${GPUSettingsState.getInstance().state.refreshIntervalMs}ms")
            stopMonitoring()
            startMonitoring()
        }
    }
    
    fun deviceInfo(): GPUDeviceInfo {
        return provider?.deviceInfo() ?: GPUDeviceInfo.UNKNOWN
    }
    
    fun latestSnapshot(): GPUSnapshot {
        return _snapshots.replayCache.lastOrNull() ?: GPUSnapshot.UNAVAILABLE
    }

    fun latestProcesses(): List<ProcessGPUUsage> {
        return _processes.replayCache.lastOrNull() ?: emptyList()
    }
    
    override fun dispose() {
        stopMonitoring()
        provider?.cleanup()
        scope.cancel()
    }
    
    companion object {
        fun getInstance(): GPUMonitoringService = service<GPUMonitoringService>()
    }
}
