package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.domain.repository.ProfileManager

class ToggleFavoriteUseCase(private val profileManager: ProfileManager) {
    suspend operator fun invoke(profile: Profile, packageName: String): Boolean {
        return profileManager.toggleFavorite(profile, packageName)
    }
}
