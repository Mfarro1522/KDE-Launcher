package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.domain.repository.SettingsManager

class DismissProductTourUseCase(private val settingsManager: SettingsManager) {
    suspend operator fun invoke() {
        settingsManager.setProductTourCompleted(true)
    }
}
