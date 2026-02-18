package dev.htmeta.mgm.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import dev.htmeta.mgm.model.GPUSnapshot
import dev.htmeta.mgm.settings.GPUSettingsState
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedDeque

@Service(Service.Level.APP)
class GPUHistoryService : Disposable {
    private val LOG = thisLogger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val history = ConcurrentLinkedDeque<GPUSnapshot>()
    
    private var collectionJob: Job? = null
    
    fun startCollecting() {
        collectionJob?.cancel()
        collectionJob = scope.launch {
            GPUMonitoringService.getInstance().snapshots.collect { snapshot ->
                if (snapshot !== GPUSnapshot.UNAVAILABLE) {
                    history.addLast(snapshot)
                    trimHistory()
                }
            }
        }
        LOG.info("GPU history collection started")
    }
    
    fun getHistory(durationMs: Long = 60_000): List<GPUSnapshot> {
        val cutoff = System.currentTimeMillis() - durationMs
        return history.filter { it.timestamp >= cutoff }.toList()
    }
    
    fun getFullHistory(): List<GPUSnapshot> = history.toList()
    
    fun clear() {
        history.clear()
    }
    
    private fun trimHistory() {
        val settings = GPUSettingsState.getInstance().state
        val retentionMs = settings.historyRetentionMinutes * 60_000L
        val cutoff = System.currentTimeMillis() - retentionMs
        while (history.peekFirst()?.let { it.timestamp < cutoff } == true) {
            history.pollFirst()
        }
    }
    
    override fun dispose() {
        collectionJob?.cancel()
        scope.cancel()
        history.clear()
    }
    
    companion object {
        fun getInstance(): GPUHistoryService = service<GPUHistoryService>()
    }
}
