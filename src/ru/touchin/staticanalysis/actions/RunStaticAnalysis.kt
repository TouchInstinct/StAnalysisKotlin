package ru.touchin.staticanalysis.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import ru.touchin.staticanalysis.tasks.AnalysisTask

class RunStaticAnalysis : AnAction("Touchin static analysis") {

    private lateinit var analysisTask: AnalysisTask

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.getData(PlatformDataKeys.PROJECT)!!
        analysisTask = AnalysisTask(project)
        analysisTask.queue()
    }

    override fun update(actionEvent: AnActionEvent) {
        actionEvent.presentation.isEnabled = !::analysisTask.isInitialized || !analysisTask.isRunning
    }

}