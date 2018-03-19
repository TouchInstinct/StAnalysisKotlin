package ru.touchin.staticanalysis.notifications

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class LogToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val consoleView = ServiceManager.getService(project, ConsoleService::class.java).consoleView
        val content = toolWindow.contentManager.factory.createContent(consoleView.component, "", true)
        toolWindow.contentManager.addContent(content)
    }

}