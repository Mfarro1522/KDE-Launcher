package dev.vive.kdelauncher.domain.repository

import android.graphics.Bitmap
import dev.vive.kdelauncher.data.IconPackInfo

interface IconPackManager {
    suspend fun getInstalledPacks(): List<IconPackInfo>
    suspend fun loadIcon(
        iconPackPackage: String,
        componentPackage: String,
        activityName: String
    ): Bitmap?
    fun clearCache()
}
