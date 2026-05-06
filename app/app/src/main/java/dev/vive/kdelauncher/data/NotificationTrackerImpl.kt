package dev.vive.kdelauncher.data

import android.service.notification.StatusBarNotification
import dev.vive.kdelauncher.domain.repository.NotificationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationTrackerImpl : NotificationTracker {

    private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()

    override fun updateCounts(notifications: Array<StatusBarNotification>?) {
        if (notifications == null) return
        val counts = mutableMapOf<String, Int>()
        for (sbn in notifications) {
            val pkg = sbn.packageName
            counts[pkg] = counts.getOrDefault(pkg, 0) + 1
        }
        _notificationCounts.value = counts
    }
}
