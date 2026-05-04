package dev.vive.kdelauncher.data

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserHandle
import android.os.UserManager

/**
 * Detects and manages Android's real Work Profile (managed profile via MDM/Enterprise).
 *
 * Android Work Profile creates a separate user (managed profile) alongside the
 * personal user. Apps in the work profile appear with a briefcase badge.
 *
 * This manager:
 * 1. Detects if the device has a real managed work profile via UserManager.
 * 2. Queries apps from the work profile using the LauncherApps API.
 * 3. Falls back gracefully if no work profile exists (device is personal-only).
 */
class WorkProfileManager(private val context: Context) {

    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    /**
     * Returns true if the device has a real Android Work Profile (managed profile).
     * This requires Android 5.0+ and an MDM/EMM setup or Google Workspace account.
     */
    fun hasRealWorkProfile(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                userManager.userProfiles.size > 1
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns the UserHandle for the managed work profile, or null if none exists.
     */
    fun getWorkProfileHandle(): UserHandle? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val myHandle = android.os.Process.myUserHandle()
                userManager.userProfiles.firstOrNull { it != myHandle }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the list of launchable apps from the real work profile.
     * Uses LauncherApps API introduced in Android 5.0.
     *
     * Returns empty list if no work profile exists or permission is denied.
     */
    fun getWorkProfileApps(): List<WorkProfileApp> {
        if (!hasRealWorkProfile()) return emptyList()

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val workHandle = getWorkProfileHandle() ?: return emptyList()

                launcherApps
                    .getActivityList(null, workHandle)
                    .map { activity ->
                        WorkProfileApp(
                            packageName = activity.applicationInfo.packageName,
                            activityName = activity.name,
                            label = activity.label.toString(),
                            userHandle = workHandle
                        )
                    }
            } else {
                emptyList()
            }
        } catch (e: SecurityException) {
            // Permission denied — work profile exists but we can't access it yet
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Launch an app from the work profile using LauncherApps + its UserHandle.
     * Regular startActivity() won't work for cross-profile launches.
     */
    fun launchWorkApp(app: WorkProfileApp): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val component = android.content.ComponentName(app.packageName, app.activityName)
                launcherApps.startMainActivity(component, app.userHandle, null, null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if the work profile is currently locked/paused.
     * When locked, work apps are not launchable until the user unlocks.
     */
    fun isWorkProfileLocked(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val workHandle = getWorkProfileHandle() ?: return false
                !launcherApps.isProfileEnabled(workHandle)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Represents an app from the real Android Work Profile.
 */
data class WorkProfileApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val userHandle: UserHandle
)
