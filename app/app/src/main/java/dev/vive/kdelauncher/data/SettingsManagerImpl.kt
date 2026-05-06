package dev.vive.kdelauncher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.domain.repository.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "launcher_settings")

class SettingsManagerImpl(private val context: Context) : SettingsManager {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val ICON_PACK = stringPreferencesKey("icon_pack")
        val SHOW_APP_LABELS = booleanPreferencesKey("show_app_labels")
        val ICON_SIZE = stringPreferencesKey("icon_size")
        val SHOW_ICON_BACKGROUND = booleanPreferencesKey("show_icon_background")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val HIDDEN_CATEGORIES = stringSetPreferencesKey("hidden_categories")
        val CATEGORY_OVERRIDES = stringSetPreferencesKey("app_category_overrides")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val LABS_ENABLED = booleanPreferencesKey("labs_enabled")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_MODEL = stringPreferencesKey("ai_model")

        fun categoryNameKey(category: AppCategory) =
            stringPreferencesKey("cat_name_${category.name}")

        fun categoryIconKey(category: AppCategory) =
            stringPreferencesKey("cat_icon_${category.name}")
    }

    // --- Theme ---

    override val darkTheme: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.DARK_THEME] ?: true }

    override suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = isDark }
    }

    // --- Icon Pack ---

    override val selectedIconPack: Flow<String?> = context.dataStore.data
        .map { it[Keys.ICON_PACK] }

    override suspend fun setSelectedIconPack(packageName: String?) {
        context.dataStore.edit {
            if (packageName == null) it.remove(Keys.ICON_PACK)
            else it[Keys.ICON_PACK] = packageName
        }
    }

    // --- App label visibility ---

    override val showAppLabels: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SHOW_APP_LABELS] ?: true }

    override suspend fun setShowAppLabels(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_APP_LABELS] = show }
    }

    // --- Icon size ---

    override val iconSize: Flow<String> = context.dataStore.data
        .map { it[Keys.ICON_SIZE] ?: "medium" }

    override suspend fun setIconSize(size: String) {
        context.dataStore.edit { it[Keys.ICON_SIZE] = size }
    }

    // --- Icon background visibility ---

    override val showIconBackground: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SHOW_ICON_BACKGROUND] ?: true }

    override suspend fun setShowIconBackground(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_ICON_BACKGROUND] = show }
    }

    // --- Grid columns ---

    override val gridColumns: Flow<Int> = context.dataStore.data
        .map { it[Keys.GRID_COLUMNS] ?: 3 }

    override suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { it[Keys.GRID_COLUMNS] = columns }
    }

    // --- Category display names ---

    override val categoryDisplayNames: Flow<Map<AppCategory, String>> = context.dataStore.data
        .map { prefs ->
            AppCategory.entries.associateWith { cat ->
                prefs[Keys.categoryNameKey(cat)] ?: cat.displayName
            }
        }

    override suspend fun setCategoryDisplayName(category: AppCategory, name: String) {
        context.dataStore.edit { it[Keys.categoryNameKey(category)] = name }
    }

    // --- Category icons ---

    override val categoryIconNames: Flow<Map<AppCategory, String>> = context.dataStore.data
        .map { prefs ->
            AppCategory.entries.associateWith { cat ->
                prefs[Keys.categoryIconKey(cat)] ?: getDefaultIconName(cat)
            }
        }

    override suspend fun setCategoryIconName(category: AppCategory, iconName: String) {
        context.dataStore.edit { it[Keys.categoryIconKey(category)] = iconName }
    }

    // --- Hidden categories ---

    override val hiddenCategories: Flow<Set<String>> = context.dataStore.data
        .map { it[Keys.HIDDEN_CATEGORIES] ?: emptySet() }

    override suspend fun setCategoryHidden(category: AppCategory, hidden: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_CATEGORIES] ?: emptySet()
            prefs[Keys.HIDDEN_CATEGORIES] = if (hidden) current + category.name
            else current - category.name
        }
    }

    // --- App category overrides ---

    override val categoryOverrides: Flow<Map<String, AppCategory>> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[Keys.CATEGORY_OVERRIDES] ?: emptySet()
            raw.mapNotNull { entry ->
                val parts = entry.split("=", limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val key = parts[0]
                val category = runCatching { AppCategory.valueOf(parts[1]) }.getOrNull()
                if (category != null) key to category else null
            }.toMap()
        }

    override suspend fun setCategoryOverride(key: String, category: AppCategory) {
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.CATEGORY_OVERRIDES] ?: emptySet()).toMutableSet()
            current.removeAll { it.startsWith("$key=") }
            current.add("$key=${category.name}")
            prefs[Keys.CATEGORY_OVERRIDES] = current
        }
    }

    override suspend fun clearCategoryOverride(key: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.CATEGORY_OVERRIDES] ?: emptySet()).toMutableSet()
            if (current.removeAll { it.startsWith("$key=") }) {
                prefs[Keys.CATEGORY_OVERRIDES] = current
            }
        }
    }

    // --- Themes & Labs ---

    override val colorTheme: Flow<String> = context.dataStore.data
        .map { it[Keys.COLOR_THEME] ?: "system" }

    override suspend fun setColorTheme(theme: String) {
        context.dataStore.edit { it[Keys.COLOR_THEME] = theme }
    }

    override val labsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.LABS_ENABLED] ?: false }

    override suspend fun setLabsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LABS_ENABLED] = enabled }
    }

    override val aiProvider: Flow<String> = context.dataStore.data
        .map { it[Keys.AI_PROVIDER] ?: "groq" }

    override suspend fun setAiProvider(provider: String) {
        context.dataStore.edit { it[Keys.AI_PROVIDER] = provider }
    }

    override val aiApiKey: Flow<String> = context.dataStore.data
        .map { it[Keys.AI_API_KEY] ?: "" }

    override suspend fun setAiApiKey(key: String) {
        context.dataStore.edit { it[Keys.AI_API_KEY] = key }
    }

    override suspend fun clearAiApiKey() {
        context.dataStore.edit { it.remove(Keys.AI_API_KEY) }
    }

    override val aiModel: Flow<String> = context.dataStore.data
        .map { it[Keys.AI_MODEL] ?: "" }

    override suspend fun setAiModel(model: String) {
        context.dataStore.edit { it[Keys.AI_MODEL] = model }
    }

    // --- Reset ---

    override suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
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
