package dev.vive.kdelauncher.domain.usecase

import android.app.Application
import android.content.Intent
import android.provider.Settings

/**
 * Opens the system's Digital Assistant / Voice Input settings.
 *
 * On Xiaomi/HyperOS devices (like Poco X7 Pro), long-press Home → Assistant
 * may stop working after changing the default launcher. The fix is a
 * user-side setting, not an app-level code change:
 *   Settings → Apps → Default Apps → Digital Assistant App → Google
 *
 * This use case navigates the user directly to the most relevant
 * settings screen, falling back gracefully across OEM variants.
 */
class OpenAssistantSettingsUseCase(private val application: Application) {

    fun openDefaultAssistantSettings(): Boolean {
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
        )
        return tryLaunchAny(intents)
    }

    fun openXiaomiButtonShortcuts(): Boolean {
        val intents = listOf(
            // Intent 1: Key and Gesture Shortcut Settings on Xiaomi/MIUI/HyperOS
            Intent().setClassName(
                "com.android.settings",
                "com.android.settings.Settings\$KeyAndGestureShortcutSettingsActivity"
            ),
            // Intent 2: System Navigation Settings on Xiaomi/MIUI/HyperOS
            Intent().setClassName(
                "com.android.settings",
                "com.android.settings.Settings\$SystemNavigationSettingsActivity"
            ),
            // Intent 3: Key/Button Shortcuts on older Xiaomi/MIUI
            Intent().setClassName(
                "com.android.settings",
                "com.android.settings.Settings\$GestureAndKeyShortcutSettingsActivity"
            ),
            // Intent 4: MIUI Full screen display settings
            Intent().setClassName(
                "com.android.settings",
                "com.android.settings.Settings\$FullScreenDisplaySettingsActivity"
            )
        )
        return tryLaunchAny(intents)
    }

    private fun tryLaunchAny(intents: List<Intent>): Boolean {
        for (intent in intents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(application.packageManager) != null) {
                try {
                    application.startActivity(intent)
                    return true
                } catch (_: Exception) {
                    // Continue to next fallback intent
                }
            }
        }
        return false
    }

    operator fun invoke() {
        if (!openDefaultAssistantSettings()) {
            val genericSettings = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                application.startActivity(genericSettings)
            } catch (_: Exception) {}
        }
    }
}
