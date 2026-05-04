package dev.vive.kdelauncher.data

import android.content.Context
import android.content.SharedPreferences
import dev.vive.kdelauncher.data.model.AppCategory

/**
 * Persists launcher settings: theme, category customization.
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("launcher_settings", Context.MODE_PRIVATE)

    // --- Theme ---

    fun isDarkTheme(): Boolean = prefs.getBoolean("dark_theme", true)

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }

    // --- Icon Pack ---

    /** Returns the package name of the selected icon pack, or null for system default. */
    fun getSelectedIconPack(): String? = prefs.getString("icon_pack", null)

    fun setSelectedIconPack(packageName: String?) {
        if (packageName == null) {
            prefs.edit().remove("icon_pack").apply()
        } else {
            prefs.edit().putString("icon_pack", packageName).apply()
        }
    }

    // --- Category display names ---

    fun getCategoryDisplayName(category: AppCategory): String {
        return prefs.getString("cat_name_${category.name}", null) ?: category.displayName
    }

    fun setCategoryDisplayName(category: AppCategory, name: String) {
        prefs.edit().putString("cat_name_${category.name}", name).apply()
    }

    // --- Category icons (stored as Material icon name) ---

    fun getCategoryIconName(category: AppCategory): String {
        return prefs.getString("cat_icon_${category.name}", null) ?: getDefaultIconName(category)
    }

    fun setCategoryIconName(category: AppCategory, iconName: String) {
        prefs.edit().putString("cat_icon_${category.name}", iconName).apply()
    }

    // --- Hidden categories ---

    fun getHiddenCategories(): Set<String> {
        return prefs.getStringSet("hidden_categories", emptySet()) ?: emptySet()
    }

    fun setCategoryHidden(category: AppCategory, hidden: Boolean) {
        val current = getHiddenCategories().toMutableSet()
        if (hidden) current.add(category.name) else current.remove(category.name)
        prefs.edit().putStringSet("hidden_categories", current).apply()
    }

    // --- Reset ---

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        fun getDefaultIconName(category: AppCategory): String = when (category) {
            AppCategory.FAVORITES -> "Star"
            AppCategory.ALL -> "GridView"
            AppCategory.DEVELOPMENT -> "Code"
            AppCategory.GRAPHICS -> "Palette"
            AppCategory.INTERNET -> "Language"
            AppCategory.GAMES -> "Gamepad"
            AppCategory.MULTIMEDIA -> "MusicNote"
            AppCategory.SYSTEM -> "Settings"
            AppCategory.UTILITIES -> "FolderOpen"
        }

        /** All available icon names for the icon picker */
        val availableIcons = listOf(
            "Star", "GridView", "Code", "Palette", "Language",
            "Gamepad", "MusicNote", "Settings", "FolderOpen",
            "Favorite", "Home", "Work", "School", "Rocket",
            "Terminal", "Cloud", "Camera", "ShoppingCart", "Bolt",
            "Diamond", "Brush", "Build", "Explore", "Forum",
            "Headphones", "LocalCafe", "Movie", "Newspaper", "Science"
        )
    }
}
