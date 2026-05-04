package dev.vive.kdelauncher.data

import android.content.Context
import android.content.SharedPreferences
import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.data.model.ProfileType

/**
 * Manages user profile state and persistence.
 * Stores favorites and profile preference in SharedPreferences.
 */
class ProfileManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kdelauncher_prefs", Context.MODE_PRIVATE)

    /**
     * Get the currently active profile.
     */
    fun getActiveProfile(): Profile {
        val type = prefs.getString("active_profile", "PERSONAL")
        return if (type == "WORK") Profile.Work else Profile.Personal
    }

    /**
     * Switch to the given profile and persist the choice.
     */
    fun setActiveProfile(profile: Profile) {
        prefs.edit().putString("active_profile", profile.type.name).apply()
    }

    /**
     * Get the set of favorite package names.
     */
    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    /**
     * Toggle favorite status for a package.
     */
    fun toggleFavorite(packageName: String): Boolean {
        val favorites = getFavorites().toMutableSet()
        val isFavorite = if (favorites.contains(packageName)) {
            favorites.remove(packageName)
            false
        } else {
            favorites.add(packageName)
            true
        }
        prefs.edit().putStringSet("favorites", favorites).apply()
        return isFavorite
    }

    /**
     * Get the set of package names tagged as "work" apps.
     */
    fun getWorkApps(): Set<String> {
        return prefs.getStringSet("work_apps", emptySet()) ?: emptySet()
    }

    /**
     * Toggle work tag for a package.
     */
    fun toggleWorkApp(packageName: String): Boolean {
        val workApps = getWorkApps().toMutableSet()
        val isWork = if (workApps.contains(packageName)) {
            workApps.remove(packageName)
            false
        } else {
            workApps.add(packageName)
            true
        }
        prefs.edit().putStringSet("work_apps", workApps).apply()
        return isWork
    }
}
