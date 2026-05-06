package dev.vive.kdelauncher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.vive.kdelauncher.TAPOLauncherApp

class LauncherNotificationService : NotificationListenerService() {

    private val notificationTracker by lazy {
        (application as TAPOLauncherApp).container.notificationTracker
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            notificationTracker.updateCounts(activeNotifications)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        try {
            notificationTracker.updateCounts(activeNotifications)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        try {
            notificationTracker.updateCounts(activeNotifications)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
