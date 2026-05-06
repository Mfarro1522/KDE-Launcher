package dev.vive.kdelauncher.domain.usecase

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import dev.vive.kdelauncher.domain.repository.WorkProfileManager

class GetSystemStatusUseCase(
    private val app: Application,
    private val workProfileManager: WorkProfileManager
) {
    data class Status(
        val isDefaultLauncher: Boolean,
        val hasRealWorkProfile: Boolean,
        val isWorkProfileLocked: Boolean,
        val isNotificationAccessGranted: Boolean
    )

    operator fun invoke(): Status {
        val isDefault = checkDefaultLauncher()
        val hasWork = workProfileManager.hasRealWorkProfile()
        val workLocked = hasWork && workProfileManager.isWorkProfileLocked()
        val notifGranted = checkNotificationAccess()
        return Status(isDefault, hasWork, workLocked, notifGranted)
    }

    private fun checkDefaultLauncher(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = app.getSystemService(Context.ROLE_SERVICE) as RoleManager
                roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            } else {
                val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                    addCategory(android.content.Intent.CATEGORY_HOME)
                }
                val resolveInfo = app.packageManager.resolveActivity(
                    intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                )
                resolveInfo?.activityInfo?.packageName == app.packageName
            }
        } catch (e: Exception) {
            true
        }
    }

    private fun checkNotificationAccess(): Boolean {
        return try {
            val pkgName = app.packageName
            val flat = android.provider.Settings.Secure.getString(
                app.contentResolver, "enabled_notification_listeners"
            )
            flat != null && flat.contains(pkgName)
        } catch (e: Exception) {
            false
        }
    }
}
