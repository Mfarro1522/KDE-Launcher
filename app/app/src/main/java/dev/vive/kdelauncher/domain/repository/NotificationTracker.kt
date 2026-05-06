package dev.vive.kdelauncher.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface NotificationTracker {
    val notificationCounts: StateFlow<Map<String, Int>>
    fun updateCounts(notifications: Array<android.service.notification.StatusBarNotification>?)
}
