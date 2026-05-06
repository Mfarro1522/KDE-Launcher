package dev.vive.kdelauncher.domain.usecase

import android.app.Application
import android.content.Intent
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.domain.repository.AppRepository
import dev.vive.kdelauncher.domain.repository.WorkProfileManager

class LaunchAppUseCase(
    private val app: Application,
    private val appRepository: AppRepository,
    private val workProfileManager: WorkProfileManager
) {
    operator fun invoke(appModel: AppModel) {
        val userHandle = appModel.userHandle
        if (userHandle != null) {
            workProfileManager.launchWorkApp(
                packageName = appModel.packageName,
                activityName = appModel.activityName,
                userHandle = userHandle
            )
            return
        }

        val intent = appRepository.getLaunchIntent(appModel.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            app.startActivity(intent)
        }
    }
}
