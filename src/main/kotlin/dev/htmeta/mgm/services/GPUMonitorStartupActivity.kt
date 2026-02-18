package dev.htmeta.mgm.services

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.htmeta.mgm.util.PlatformDetector

class GPUMonitorStartupActivity : ProjectActivity {
    private val LOG = thisLogger()
    
    override suspend fun execute(project: Project) {
        if (!PlatformDetector.isMacOS()) {
            LOG.info("Metal GPU Monitor: not macOS, skipping initialization")
            return
        }
        
        LOG.info("Metal GPU Monitor: initializing on macOS")
        
        val monitoringService = GPUMonitoringService.getInstance()
        monitoringService.startMonitoring()
        
        val historyService = GPUHistoryService.getInstance()
        historyService.startCollecting()
        
        val alertService = GPUAlertService.getInstance()
        alertService.startMonitoring()
        
        LOG.info("Metal GPU Monitor: all services started")
    }
}
