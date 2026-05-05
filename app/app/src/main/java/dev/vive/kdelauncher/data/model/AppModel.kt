package dev.vive.kdelauncher.data.model

import android.graphics.Bitmap
import android.os.UserHandle

/**
 * Represents a single launchable application on the device.
 * Uses Bitmap instead of Drawable to avoid repeated toBitmap() conversions.
 */
data class AppModel(
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconBitmap: Bitmap?,
    val category: AppCategory = AppCategory.ALL,
    val isFavorite: Boolean = false,
    val profileTag: ProfileType = ProfileType.PERSONAL,
    val userHandle: UserHandle? = null,
    val notificationCount: Int = 0
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

    private val packagePatterns = mapOf(
        // Development
        "termux" to AppCategory.DEVELOPMENT,
        "terminal" to AppCategory.DEVELOPMENT,
        "com.termux" to AppCategory.DEVELOPMENT,
        "editor" to AppCategory.DEVELOPMENT,
        "ide" to AppCategory.DEVELOPMENT,
        "code" to AppCategory.DEVELOPMENT,
        "github" to AppCategory.DEVELOPMENT,
        "gitlab" to AppCategory.DEVELOPMENT,
        "docker" to AppCategory.DEVELOPMENT,
        "database" to AppCategory.DEVELOPMENT,
        "sql" to AppCategory.DEVELOPMENT,
        "dev" to AppCategory.DEVELOPMENT,

        // Graphics
        "gallery" to AppCategory.GRAPHICS,
        "photo" to AppCategory.GRAPHICS,
        "camera" to AppCategory.GRAPHICS,
        "draw" to AppCategory.GRAPHICS,
        "paint" to AppCategory.GRAPHICS,
        "image" to AppCategory.GRAPHICS,
        "sketch" to AppCategory.GRAPHICS,
        "canva" to AppCategory.GRAPHICS,
        "snapseed" to AppCategory.GRAPHICS,

        // Internet
        "browser" to AppCategory.INTERNET,
        "chrome" to AppCategory.INTERNET,
        "firefox" to AppCategory.INTERNET,
        "brave" to AppCategory.INTERNET,
        "mail" to AppCategory.INTERNET,
        "email" to AppCategory.INTERNET,
        "gmail" to AppCategory.INTERNET,
        "slack" to AppCategory.INTERNET,
        "telegram" to AppCategory.INTERNET,
        "whatsapp" to AppCategory.INTERNET,
        "discord" to AppCategory.INTERNET,
        "twitter" to AppCategory.INTERNET,
        "reddit" to AppCategory.INTERNET,
        "instagram" to AppCategory.INTERNET,
        "messenger" to AppCategory.INTERNET,
        "signal" to AppCategory.INTERNET,

        // Games
        "game" to AppCategory.GAMES,
        "play" to AppCategory.GAMES,

        // Multimedia
        "music" to AppCategory.MULTIMEDIA,
        "spotify" to AppCategory.MULTIMEDIA,
        "video" to AppCategory.MULTIMEDIA,
        "youtube" to AppCategory.MULTIMEDIA,
        "vlc" to AppCategory.MULTIMEDIA,
        "podcast" to AppCategory.MULTIMEDIA,
        "player" to AppCategory.MULTIMEDIA,
        "audio" to AppCategory.MULTIMEDIA,
        "netflix" to AppCategory.MULTIMEDIA,
        "tiktok" to AppCategory.MULTIMEDIA,

        // System
        "settings" to AppCategory.SYSTEM,
        "monitor" to AppCategory.SYSTEM,
        "filemanager" to AppCategory.SYSTEM,
        "files" to AppCategory.SYSTEM,
        "manager" to AppCategory.SYSTEM,
        "updater" to AppCategory.SYSTEM,

        // Utilities
        "calculator" to AppCategory.UTILITIES,
        "calc" to AppCategory.UTILITIES,
        "clock" to AppCategory.UTILITIES,
        "alarm" to AppCategory.UTILITIES,
        "calendar" to AppCategory.UTILITIES,
        "notes" to AppCategory.UTILITIES,
        "weather" to AppCategory.UTILITIES,
        "maps" to AppCategory.UTILITIES,
        "translate" to AppCategory.UTILITIES,
        "compass" to AppCategory.UTILITIES,
        "flashlight" to AppCategory.UTILITIES,
        "recorder" to AppCategory.UTILITIES,
        "contacts" to AppCategory.UTILITIES,
        "phone" to AppCategory.UTILITIES,
        "dialer" to AppCategory.UTILITIES,
        "messages" to AppCategory.UTILITIES,
        "sms" to AppCategory.UTILITIES,
    )

    /**
     * Categorize an app based on its package name and Android category info.
     */
    fun categorize(packageName: String, androidCategory: Int): AppCategory {
        val lowerPkg = packageName.lowercase()

        // Check package name patterns
        for ((pattern, category) in packagePatterns) {
            if (lowerPkg.contains(pattern)) {
                return category
            }
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
}
