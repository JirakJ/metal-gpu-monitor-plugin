package dev.htmeta.mgm.ui.toolwindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import dev.htmeta.mgm.util.PlatformDetector

class GPUToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun isApplicable(project: Project): Boolean = PlatformDetector.isMacOS()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = GPUToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "Monitor", false)
        toolWindow.contentManager.addContent(content)
        @Suppress("SSBasedInspection")
        toolWindow.disposable.let { parent ->
            com.intellij.openapi.util.Disposer.register(parent, panel)
        }
    }
}
