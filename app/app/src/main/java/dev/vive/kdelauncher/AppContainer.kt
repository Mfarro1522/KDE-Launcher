package dev.vive.kdelauncher

import android.app.Application
import dev.vive.kdelauncher.data.IconPackManager as IconPackManagerImpl
import dev.vive.kdelauncher.data.NotificationTrackerImpl
import dev.vive.kdelauncher.data.ProfileManagerImpl
import dev.vive.kdelauncher.data.SettingsManagerImpl
import dev.vive.kdelauncher.data.WorkProfileManager as WorkProfileManagerImpl
import dev.vive.kdelauncher.data.repository.AppRepositoryImpl
import dev.vive.kdelauncher.domain.repository.AppRepository
import dev.vive.kdelauncher.domain.repository.IconPackManager
import dev.vive.kdelauncher.domain.repository.NotificationTracker
import dev.vive.kdelauncher.domain.repository.ProfileManager
import dev.vive.kdelauncher.domain.repository.SettingsManager
import dev.vive.kdelauncher.domain.repository.WorkProfileManager
import dev.vive.kdelauncher.domain.usecase.GetSystemStatusUseCase
import dev.vive.kdelauncher.domain.usecase.LaunchAppUseCase
import dev.vive.kdelauncher.domain.usecase.LoadAppsUseCase
import dev.vive.kdelauncher.domain.usecase.LoadIconPacksUseCase
import dev.vive.kdelauncher.domain.usecase.SetCategoryOverrideUseCase
import dev.vive.kdelauncher.domain.usecase.ToggleFavoriteUseCase
import dev.vive.kdelauncher.domain.usecase.ToggleWorkAppUseCase

/**
 * Manual dependency injection container.
 *
 * Creates and holds singleton instances of all repositories, managers and use cases.
 * This is a pragmatic replacement for Hilt while we resolve the javapoet classpath issue.
 */
class AppContainer(private val application: Application) {

    val appRepository: AppRepository = AppRepositoryImpl(application)
    val profileManager: ProfileManager = ProfileManagerImpl(application)
    val settingsManager: SettingsManager = SettingsManagerImpl(application)
    val iconPackManager: IconPackManager = IconPackManagerImpl(application)
    val workProfileManager: WorkProfileManager = WorkProfileManagerImpl(application)
    val notificationTracker: NotificationTracker = NotificationTrackerImpl()

    val loadAppsUseCase = LoadAppsUseCase(appRepository, workProfileManager)
    val launchAppUseCase = LaunchAppUseCase(application, appRepository, workProfileManager)
    val toggleFavoriteUseCase = ToggleFavoriteUseCase(profileManager)
    val toggleWorkAppUseCase = ToggleWorkAppUseCase(profileManager)
    val loadIconPacksUseCase = LoadIconPacksUseCase(iconPackManager)
    val getSystemStatusUseCase = GetSystemStatusUseCase(application, workProfileManager)
    val setCategoryOverrideUseCase = SetCategoryOverrideUseCase(settingsManager)
}
