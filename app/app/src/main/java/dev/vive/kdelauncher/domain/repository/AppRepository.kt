package dev.vive.kdelauncher.domain.repository

import android.content.Intent
import android.graphics.Bitmap
import dev.vive.kdelauncher.data.model.AppModel

interface AppRepository {
    suspend fun getInstalledApps(selectedIconPack: String? = null): List<AppModel>
    suspend fun getInstalledAppsMetadata(): List<AppModel>
    fun getLaunchIntent(packageName: String): Intent?
    suspend fun getAppIcon(
        packageName: String,
        activityName: String,
        selectedIconPack: String?
    ): Bitmap?
    fun clearIconPackCache()
}
