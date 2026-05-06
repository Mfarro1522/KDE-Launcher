package dev.vive.kdelauncher.domain.usecase

import android.app.Application
import android.content.Intent
import android.net.Uri
import dev.vive.kdelauncher.data.model.AppModel

class UninstallAppUseCase(private val application: Application) {

    operator fun invoke(app: AppModel) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        application.startActivity(intent)
    }
}
