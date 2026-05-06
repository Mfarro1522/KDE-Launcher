package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.domain.repository.SettingsManager

class SetCategoryOverrideUseCase(private val settingsManager: SettingsManager) {
    suspend operator fun invoke(key: String, category: AppCategory) {
        settingsManager.setCategoryOverride(key, category)
    }

    suspend fun clear(key: String) {
        settingsManager.clearCategoryOverride(key)
    }
}
