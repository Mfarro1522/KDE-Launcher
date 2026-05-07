package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.domain.repository.SettingsManager
import kotlinx.coroutines.flow.Flow

class CheckProductTourStatusUseCase(private val settingsManager: SettingsManager) {
    operator fun invoke(): Flow<Boolean> = settingsManager.productTourCompleted
}
