package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.domain.repository.ProfileManager

class ToggleFavoriteUseCase(private val profileManager: ProfileManager) {
    suspend operator fun invoke(packageName: String): Boolean {
        return profileManager.toggleFavorite(packageName)
    }
}
