package ru.touchin.staticanalysis.tasks

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.AppIcon
import ru.touchin.staticanalysis.notifications.ConsoleService
import ru.touchin.staticanalysis.notifications.PluginNotificationManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.regex.Pattern


class AnalysisTask(project: Project) : Backgroundable(project, "StaticAnalysis", true) {

    var isRunning: Boolean = false
    private val notificationsManager = PluginNotificationManager(project)

    private val gradlewCommand = if (System.getProperty("os.name").startsWith("Windows"))
        listOf("cmd", "/c", "gradlew.bat", "staticAnalys")
    else
        listOf("./gradlew", "staticAnalys")

    private val gradlewProcess: Process by lazy {
        ProcessBuilder(gradlewCommand)
                .directory(File(project.basePath))
                .redirectErrorStream(true)
                .start()
    }

    override fun run(progressIndicator: ProgressIndicator) {
        isRunning = true
        progressIndicator.isIndeterminate = true
        try {
            runAnalysis(progressIndicator)
        } catch (canceledException: ProcessCanceledException) {
            progressIndicator.cancel()
        } catch (exception: Exception) {
            notificationsManager.showErrorNotification("Exception: " + exception.message)
        }
    }

    override fun onCancel() {
        gradlewProcess.destroy()
    }

    override fun onFinished() {
        isRunning = false
    }

    @Throws(Exception::class)
    private fun runAnalysis(progressIndicator: ProgressIndicator) {
        val analysisOutput: String = getAnalysisOutput(progressIndicator)
        if (!analysisOutput.startsWith("Error") && !analysisOutput.startsWith("FAILURE")) {
            if (Pattern.compile("Overall: PASSED").matcher(analysisOutput).find()) {
                notificationsManager.showInfoNotification("Overall: PASSED!")
                requestIdeFocus()
            } else if (!Pattern.compile("Overall: FAILED").matcher(analysisOutput).find()) {
                notificationsManager.showErrorNotification("Can't detect analysis result. Try to run it manually.")
            } else {
                val errorsCountPattern = Pattern.compile("Overall: FAILED \\((.+)\\)")
                val errorsCountMatcher = errorsCountPattern.matcher(analysisOutput)
                if (errorsCountMatcher.find()) {
                    notificationsManager.showErrorNotification(String.format("Analysis failed: %s", errorsCountMatcher.group(1)))
                    ApplicationManager.getApplication().invokeLater {
                        ToolWindowManager.getInstance(project).getToolWindow("Static Analysis Log").show(null)
                    }
                } else {
                    notificationsManager.showErrorNotification("Can't detect analysis result. Try to run it manually.")
                }
            }
        } else {
            notificationsManager.showErrorNotification(analysisOutput)
        }
    }

    @Throws(Exception::class)
    private fun getAnalysisOutput(progressIndicator: ProgressIndicator): String {
        val bufferedReader = BufferedReader(InputStreamReader(gradlewProcess.inputStream))
        val analysisOutputBuilder = StringBuilder()

        val consoleView = ServiceManager.getService(project, ConsoleService::class.java).consoleView
        var outputLine: String? = bufferedReader.readLine()
        while (outputLine != null) {

            consoleView.print(outputLine + '\n', ConsoleViewContentType.NORMAL_OUTPUT)
            progressIndicator.text2 = outputLine
            progressIndicator.checkCanceled()
            analysisOutputBuilder.append(outputLine)
            analysisOutputBuilder.append('\n')
            outputLine = bufferedReader.readLine()
        }

        return analysisOutputBuilder.toString()
    }

    private fun requestIdeFocus() {
        ApplicationManager.getApplication().invokeLater {
            val frame = WindowManager.getInstance().getFrame(project)
            if (frame is IdeFrame) {
                AppIcon.getInstance().requestFocus(frame)
                AppIcon.getInstance().requestAttention(project, true)
                AppIcon.getInstance().setOkBadge(project, true)
            }
        }
    }

}