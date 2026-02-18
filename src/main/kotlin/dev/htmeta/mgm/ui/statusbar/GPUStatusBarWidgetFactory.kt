package dev.htmeta.mgm.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import dev.htmeta.mgm.util.PlatformDetector

class GPUStatusBarWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = WIDGET_ID

    override fun getDisplayName(): String = "GPU Monitor"

    override fun isAvailable(project: Project): Boolean = PlatformDetector.isMacOS()

    override fun createWidget(project: Project): StatusBarWidget = GPUStatusBarWidget(project)

    companion object {
        const val WIDGET_ID = "dev.htmeta.mgm.statusBar"
    }
}
