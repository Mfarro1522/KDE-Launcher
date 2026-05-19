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

    operator fun invoke() {
        // Priority order: most specific → most generic.
        // Different OEMs expose the assistant setting under different intents.
        val intents = listOf(
            // Android 10+ default apps screen (closest to "Digital Assistant")
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            // Voice input settings (older Android versions)
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            // Generic settings as last resort
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in intents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(application.packageManager) != null) {
                application.startActivity(intent)
                return
            }
        }
    }
}
