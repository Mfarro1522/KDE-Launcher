package dev.vive.kdelauncher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

/**
 * Transparent trampoline activity that opens the system "Choose Default Home App"
 * settings screen and immediately finishes itself.
 *
 * On Android 10+ (API 29+) we can deep-link directly to the Home App chooser.
 * On older Android versions we open the general Default Apps settings.
 *
 * Usage: start this activity from anywhere inside the launcher to let the user
 * set KDE Launcher as their default home screen.
 */
class SetDefaultLauncherActivity : Activity() {

    companion object {
        const val EXTRA_OPEN_SETTINGS = "open_settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openSettings = intent?.getBooleanExtra(EXTRA_OPEN_SETTINGS, false) ?: false
        if (openSettings && !isAlreadyDefaultLauncher()) {
            openDefaultLauncherSettings()
        } else {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
        finish()
    }

    private fun isAlreadyDefaultLauncher(): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(ROLE_SERVICE) as android.app.role.RoleManager
                roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)
            } else {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }
                val resolveInfo = packageManager.resolveActivity(
                    intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                )
                resolveInfo?.activityInfo?.packageName == packageName
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun openDefaultLauncherSettings() {
        try {
            // Android 10+ has a direct deep-link to the Home App chooser
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return
            }
        } catch (_: Exception) {}

        // Fallback: open general Default Apps settings
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (_: Exception) {
            // Last resort: open general settings
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
