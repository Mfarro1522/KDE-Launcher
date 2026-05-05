package dev.vive.kdelauncher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationTracker {
    // Maps packageName to notification count
    private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()

    fun updateCounts(notifications: Array<StatusBarNotification>?) {
        if (notifications == null) return
        val counts = mutableMapOf<String, Int>()
        for (sbn in notifications) {
            val pkg = sbn.packageName
            counts[pkg] = counts.getOrDefault(pkg, 0) + 1
        }
        _notificationCounts.value = counts
    }
}

class LauncherNotificationService : NotificationListenerService() {
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            NotificationTracker.updateCounts(activeNotifications)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        try {
            NotificationTracker.updateCounts(activeNotifications)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        try {
            NotificationTracker.updateCounts(activeNotifications)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
