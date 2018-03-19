package ru.touchin.staticanalysis.notifications

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project

class ConsoleService(project: Project) {

    val consoleView: ConsoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console

}