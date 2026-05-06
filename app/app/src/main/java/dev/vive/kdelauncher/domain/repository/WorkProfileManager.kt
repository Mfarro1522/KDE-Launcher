package dev.vive.kdelauncher.domain.repository

import android.graphics.Bitmap
import android.os.UserHandle
import dev.vive.kdelauncher.data.WorkProfileApp

interface WorkProfileManager {
    fun hasRealWorkProfile(): Boolean
    fun getWorkProfileHandle(): UserHandle?
    fun getWorkProfileApps(loadIcons: Boolean = true): List<WorkProfileApp>
    fun launchWorkApp(
        packageName: String,
        activityName: String,
        userHandle: UserHandle
    ): Boolean
    fun loadWorkAppIcon(
        packageName: String,
        activityName: String,
        userHandle: UserHandle
    ): Bitmap?
    fun isWorkProfileLocked(): Boolean
}
