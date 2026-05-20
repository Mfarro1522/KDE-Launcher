package dev.vive.kdelauncher.domain.usecase

import android.app.Application
import android.content.Intent
import dev.vive.kdelauncher.SetDefaultLauncherActivity

class OpenSetDefaultLauncherUseCase(private val application: Application) {

    operator fun invoke() {
        val intent = Intent(application, SetDefaultLauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(SetDefaultLauncherActivity.EXTRA_OPEN_SETTINGS, true)
        }
        application.startActivity(intent)
    }
}
