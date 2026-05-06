package dev.vive.kdelauncher.data.model

import android.graphics.Bitmap
import android.os.UserHandle
import androidx.compose.runtime.Immutable

/**
 * Immutable wrapper for Bitmap so Compose treats it as stable.
 * Without this, Compose assumes Bitmap is mutable and recomposes
 * every AppIcon on every state change.
 */
@Immutable
data class AppIconBitmap(val bitmap: Bitmap) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(bitmap)
}

/**
 * Represents a single launchable application on the device.
 */
data class AppModel(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: AppIconBitmap? = null,
    val category: AppCategory = AppCategory.ALL,
    val isFavorite: Boolean = false,
    val profileTag: ProfileType = ProfileType.PERSONAL,
    val userHandle: UserHandle? = null
)

/**
 * App categories inspired by KDE's menu structure.
 * Each category has a display label and an associated Material icon name.
 */
enum class AppCategory(val displayName: String) {
    FAVORITES("Favoritos"),
    ALL("Todas"),
    DEVELOPMENT("Desarrollo"),
    GRAPHICS("Gráficos"),
    INTERNET("Internet"),
    GAMES("Juegos"),
    MULTIMEDIA("Multimedia"),
    SYSTEM("Sistema"),
    UTILITIES("Utilidades")
}

/**
 * Categorization rules based on Android's ApplicationInfo.category
 * and common package name patterns.
 */
object AppCategorizer {

    private data class CategoryRule(
        val pattern: String,
        val category: AppCategory,
        val baseScore: Int = 10
    )

    private val exactPackageCategories = mapOf(
        "com.android.vending" to AppCategory.INTERNET,
        "com.facebook.pages.app" to AppCategory.INTERNET,
        "com.google.android.apps.photos" to AppCategory.GRAPHICS,
        "com.google.android.youtube" to AppCategory.MULTIMEDIA,
        "com.google.android.play.games" to AppCategory.GAMES
    )

    private val categoryRules = listOf(
        // Development
        CategoryRule("termux", AppCategory.DEVELOPMENT, baseScore = 20),
        CategoryRule("terminal", AppCategory.DEVELOPMENT),
        CategoryRule("editor", AppCategory.DEVELOPMENT),
        CategoryRule("ide", AppCategory.DEVELOPMENT),
        CategoryRule("code", AppCategory.DEVELOPMENT),
        CategoryRule("github", AppCategory.DEVELOPMENT),
        CategoryRule("gitlab", AppCategory.DEVELOPMENT),
        CategoryRule("docker", AppCategory.DEVELOPMENT),
        CategoryRule("database", AppCategory.DEVELOPMENT),
        CategoryRule("sql", AppCategory.DEVELOPMENT),
        CategoryRule("dev", AppCategory.DEVELOPMENT, baseScore = 6),

        // Graphics
        CategoryRule("gallery", AppCategory.GRAPHICS),
        CategoryRule("photo", AppCategory.GRAPHICS),
        CategoryRule("photos", AppCategory.GRAPHICS, baseScore = 14),
        CategoryRule("camera", AppCategory.GRAPHICS),
        CategoryRule("draw", AppCategory.GRAPHICS),
        CategoryRule("paint", AppCategory.GRAPHICS),
        CategoryRule("image", AppCategory.GRAPHICS),
        CategoryRule("sketch", AppCategory.GRAPHICS),
        CategoryRule("canva", AppCategory.GRAPHICS),
        CategoryRule("snapseed", AppCategory.GRAPHICS, baseScore = 16),

        // Internet
        CategoryRule("browser", AppCategory.INTERNET),
        CategoryRule("chrome", AppCategory.INTERNET),
        CategoryRule("firefox", AppCategory.INTERNET),
        CategoryRule("brave", AppCategory.INTERNET),
        CategoryRule("mail", AppCategory.INTERNET),
        CategoryRule("email", AppCategory.INTERNET),
        CategoryRule("gmail", AppCategory.INTERNET),
        CategoryRule("slack", AppCategory.INTERNET),
        CategoryRule("telegram", AppCategory.INTERNET),
        CategoryRule("whatsapp", AppCategory.INTERNET),
        CategoryRule("discord", AppCategory.INTERNET),
        CategoryRule("twitter", AppCategory.INTERNET),
        CategoryRule("reddit", AppCategory.INTERNET),
        CategoryRule("instagram", AppCategory.INTERNET),
        CategoryRule("messenger", AppCategory.INTERNET),
        CategoryRule("signal", AppCategory.INTERNET),
        CategoryRule("facebook", AppCategory.INTERNET),
        CategoryRule("pages", AppCategory.INTERNET),
        CategoryRule("store", AppCategory.INTERNET),
        CategoryRule("vending", AppCategory.INTERNET, baseScore = 14),

        // Games
        CategoryRule("game", AppCategory.GAMES, baseScore = 14),
        CategoryRule("games", AppCategory.GAMES, baseScore = 16),
        CategoryRule("play", AppCategory.GAMES, baseScore = 5),

        // Multimedia
        CategoryRule("music", AppCategory.MULTIMEDIA, baseScore = 14),
        CategoryRule("spotify", AppCategory.MULTIMEDIA, baseScore = 16),
        CategoryRule("video", AppCategory.MULTIMEDIA, baseScore = 14),
        CategoryRule("youtube", AppCategory.MULTIMEDIA, baseScore = 16),
        CategoryRule("vlc", AppCategory.MULTIMEDIA, baseScore = 16),
        CategoryRule("podcast", AppCategory.MULTIMEDIA, baseScore = 14),
        CategoryRule("player", AppCategory.MULTIMEDIA, baseScore = 16),
        CategoryRule("audio", AppCategory.MULTIMEDIA, baseScore = 14),
        CategoryRule("netflix", AppCategory.MULTIMEDIA, baseScore = 16),
        CategoryRule("tiktok", AppCategory.MULTIMEDIA, baseScore = 14),

        // System
        CategoryRule("settings", AppCategory.SYSTEM, baseScore = 18),
        CategoryRule("monitor", AppCategory.SYSTEM),
        CategoryRule("filemanager", AppCategory.SYSTEM),
        CategoryRule("files", AppCategory.SYSTEM),
        CategoryRule("manager", AppCategory.SYSTEM, baseScore = 8),
        CategoryRule("updater", AppCategory.SYSTEM),

        // Utilities
        CategoryRule("calculator", AppCategory.UTILITIES, baseScore = 14),
        CategoryRule("calc", AppCategory.UTILITIES),
        CategoryRule("clock", AppCategory.UTILITIES),
        CategoryRule("alarm", AppCategory.UTILITIES),
        CategoryRule("calendar", AppCategory.UTILITIES),
        CategoryRule("notes", AppCategory.UTILITIES),
        CategoryRule("weather", AppCategory.UTILITIES),
        CategoryRule("maps", AppCategory.UTILITIES, baseScore = 14),
        CategoryRule("translate", AppCategory.UTILITIES),
        CategoryRule("compass", AppCategory.UTILITIES),
        CategoryRule("flashlight", AppCategory.UTILITIES),
        CategoryRule("recorder", AppCategory.UTILITIES),
        CategoryRule("contacts", AppCategory.UTILITIES),
        CategoryRule("phone", AppCategory.UTILITIES),
        CategoryRule("dialer", AppCategory.UTILITIES),
        CategoryRule("messages", AppCategory.UTILITIES),
        CategoryRule("sms", AppCategory.UTILITIES),
    )

    /**
     * Categorize an app based on its package name and Android category info.
     */
    fun categorize(packageName: String, androidCategory: Int): AppCategory {
        val lowerPkg = packageName.lowercase()
        exactPackageCategories[lowerPkg]?.let { return it }

        val bestMatch = categoryRules
            .mapNotNull { rule ->
                scoreRule(lowerPkg, rule)?.let { rule.category to it }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, scores) -> scores.maxOrNull() ?: 0 }
            .maxByOrNull { it.value }

        if (bestMatch != null && bestMatch.value > 0) {
            return bestMatch.key
        }

        // Fall back to Android's built-in category
        return when (androidCategory) {
            android.content.pm.ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            android.content.pm.ApplicationInfo.CATEGORY_AUDIO -> AppCategory.MULTIMEDIA
            android.content.pm.ApplicationInfo.CATEGORY_VIDEO -> AppCategory.MULTIMEDIA
            android.content.pm.ApplicationInfo.CATEGORY_IMAGE -> AppCategory.GRAPHICS
            android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.INTERNET
            android.content.pm.ApplicationInfo.CATEGORY_NEWS -> AppCategory.INTERNET
            android.content.pm.ApplicationInfo.CATEGORY_MAPS -> AppCategory.UTILITIES
            android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.UTILITIES
            else -> AppCategory.ALL
        }
    }

    private fun scoreRule(packageName: String, rule: CategoryRule): Int? {
        if (!packageName.contains(rule.pattern)) return null

        val exactTokenMatch = packageName
            .split('.', '_', '-', '/')
            .any { token -> token == rule.pattern }

        val exactTokenBonus = if (exactTokenMatch) 20 else 0
        val substringBonus = rule.pattern.length
        return rule.baseScore + exactTokenBonus + substringBonus
    }
}
