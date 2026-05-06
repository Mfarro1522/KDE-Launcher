package dev.vive.kdelauncher.data

import android.content.Context
import android.content.SharedPreferences
import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.data.model.ProfileType
import dev.vive.kdelauncher.domain.repository.ProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user profile state and persistence.
 * Stores favorites and profile preference in SharedPreferences.
 *
 * Implements [ProfileManager] interface and exposes reactive [StateFlow]
 * properties so the ViewModel no longer needs to manually refresh values.
 */
class ProfileManagerImpl(context: Context) : ProfileManager {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kdelauncher_prefs", Context.MODE_PRIVATE)

    private val _activeProfile = MutableStateFlow(readActiveProfile())
    override val activeProfile: StateFlow<Profile> = _activeProfile.asStateFlow()

    private val _favorites = MutableStateFlow(readFavorites())
    override val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _workApps = MutableStateFlow(readWorkApps())
    override val workApps: StateFlow<Set<String>> = _workApps.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "active_profile" -> _activeProfile.value = readActiveProfile()
            "favorites" -> _favorites.value = readFavorites()
            "work_apps" -> _workApps.value = readWorkApps()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override suspend fun setActiveProfile(profile: Profile) {
        prefs.edit().putString("active_profile", profile.type.name).apply()
        _activeProfile.value = profile
    }

    override suspend fun toggleFavorite(packageName: String): Boolean {
        val current = _favorites.value.toMutableSet()
        val isFavorite = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet("favorites", current).apply()
        _favorites.value = current
        return isFavorite
    }

    override suspend fun toggleWorkApp(packageName: String): Boolean {
        val current = _workApps.value.toMutableSet()
        val isWork = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet("work_apps", current).apply()
        _workApps.value = current
        return isWork
    }

    private fun readActiveProfile(): Profile {
        val type = prefs.getString("active_profile", "PERSONAL")
        return if (type == "WORK") Profile.Work else Profile.Personal
    }

    private fun readFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    private fun readWorkApps(): Set<String> {
        return prefs.getStringSet("work_apps", emptySet()) ?: emptySet()
    }
}
