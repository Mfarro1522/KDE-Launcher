package dev.vive.kdelauncher.domain.repository

import dev.vive.kdelauncher.data.model.Profile
import kotlinx.coroutines.flow.StateFlow

interface ProfileManager {
    val activeProfile: StateFlow<Profile>
    suspend fun setActiveProfile(profile: Profile)
    val favorites: StateFlow<Set<String>>
    suspend fun toggleFavorite(packageName: String): Boolean
    val workApps: StateFlow<Set<String>>
    suspend fun toggleWorkApp(packageName: String): Boolean
}
