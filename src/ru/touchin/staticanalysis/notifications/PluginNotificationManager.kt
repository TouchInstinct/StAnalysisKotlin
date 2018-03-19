package ru.touchin.staticanalysis.notifications

import com.intellij.notification.*
import com.intellij.openapi.project.Project
import icons.PluginIcons

class PluginNotificationManager(private val project: Project) {

    fun showErrorNotification(message: String) {
        hideOldNotifications()
        NotificationGroup(NOTIFICATION_TITLE, NotificationDisplayType.STICKY_BALLOON, true)
                .createNotification(NOTIFICATION_TITLE, message, NotificationType.ERROR, null)
                .notify(project)
    }

    fun showInfoNotification(message: String) {
        hideOldNotifications()
        NotificationGroup(NOTIFICATION_TITLE, NotificationDisplayType.STICKY_BALLOON, true, null, PluginIcons.PLUGIN_ICON)
        Notification(NOTIFICATION_TITLE, PluginIcons.PLUGIN_ICON, NOTIFICATION_TITLE, null, message, NotificationType.INFORMATION, null)
                .notify(project)
    }

    private fun hideOldNotifications() {
        val logModel = EventLog.getLogModel(project)
        for (notification in logModel.notifications) {
            if (notification.groupId == NOTIFICATION_TITLE) {
                logModel.removeNotification(notification)
                notification.expire()
            }
        }
    }

    companion object {
        private const val NOTIFICATION_TITLE = "Static Analysis"
    }

}