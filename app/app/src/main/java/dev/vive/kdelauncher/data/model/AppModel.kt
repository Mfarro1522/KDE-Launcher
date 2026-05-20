package dev.vive.kdelauncher.data.model

import android.graphics.Bitmap
import android.os.UserHandle
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Immutable wrapper for Bitmap so Compose treats it as stable.
 * Without this, Compose assumes Bitmap is mutable and recomposes
 * every AppIcon on every state change.
 */
@Immutable
data class AppIconBitmap(val bitmap: Bitmap) {
    /** Pre-computed ImageBitmap — avoids allocating a new wrapper per recomposition. */
    val imageBitmap by lazy { bitmap.asImageBitmap() }

    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(bitmap)
}

/**
 * Represents a single launchable application on the device.
 *
 * Marked @Immutable because all fields are set at construction and never
 * mutated. This lets Compose treat instances as stable and skip
 * recomposition when the same reference is passed.
 */
@Immutable
data class AppModel(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: AppIconBitmap? = null,
    val category: String = AppCategory.ALL,
    val isFavorite: Boolean = false,
    val profileTag: ProfileType = ProfileType.PERSONAL,
    val userHandle: UserHandle? = null,
    val versionCode: Long = 0L,
    val isSystemApp: Boolean = false
)

/**
 * Fixed categories shown by default in the launcher UI.
 */
object AppCategory {
    const val FAVORITES = "favorites"
    const val ALL = "all"
    const val COMPRAS = "compras"
    const val MULTIMEDIA = "multimedia"
    const val SYSTEM = "system"
    const val HERRAMIENTAS = "herramientas"
    const val GAMES = "games"

    /** Categories always visible in settings / tabs */
    val FIXED = listOf(FAVORITES, ALL, COMPRAS, MULTIMEDIA, SYSTEM, HERRAMIENTAS, GAMES)

    /** Categories handled locally and excluded from AI payload. */
    val AI_EXCLUDED = setOf(SYSTEM, GAMES, MULTIMEDIA)

    /** Human-readable labels (Spanish). Dynamic categories fallback to capitalized ID. */
    fun displayName(id: String): String = when (id) {
        FAVORITES -> "Favoritos"
        ALL -> "Todas"
        COMPRAS -> "Compras"
        MULTIMEDIA -> "Multimedia"
        SYSTEM -> "Apps del Sistema"
        HERRAMIENTAS -> "Herramientas"
        GAMES -> "Juegos"
        else -> id.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    /** Pre-selected vector assets from Material Design Icons catalog. */
    fun defaultIcon(id: String): String = when (id) {
        FAVORITES -> "Star"
        ALL -> "GridView"
        COMPRAS -> "ShoppingCart"
        MULTIMEDIA -> "Headphones"
        SYSTEM -> "Settings"
        HERRAMIENTAS -> "Build"
        GAMES -> "Gamepad"
        else -> "Folder"
    }

    val availableIcons = listOf(
        "Star", "GridView", "Code", "Palette", "Language",
        "Gamepad", "MusicNote", "Settings", "FolderOpen",
        "Favorite", "Home", "Work", "School", "Rocket",
        "Terminal", "Cloud", "Camera", "ShoppingCart", "Bolt",
        "Diamond", "Brush", "Build", "Explore", "Forum",
        "Headphones", "LocalCafe", "Movie", "Newspaper", "Science"
    )

    // For test and general backward-compatibility/iterability
    val entries: List<String> = FIXED
}

object AppCategorizer {

    private val exactPackageCategories = mapOf(
        // Compras
        "com.bcp.innovacxion.yapeapp" to AppCategory.COMPRAS,
        "pe.interbank.mobilebanking" to AppCategory.COMPRAS,
        "com.bbva.nxt_peru" to AppCategory.COMPRAS,
        "ar.com.uala" to AppCategory.COMPRAS,
        "com.nubank" to AppCategory.COMPRAS,
        "com.mercadopago.wallet" to AppCategory.COMPRAS,
        "com.rappi" to AppCategory.COMPRAS,
        "com.pedidosya" to AppCategory.COMPRAS,
        "com.cornershopapp.shopper" to AppCategory.COMPRAS,
        "pe.plin" to AppCategory.COMPRAS,

        // Multimedia
        "com.google.android.youtube" to AppCategory.MULTIMEDIA,
        "com.netflix.mediaclient" to AppCategory.MULTIMEDIA,
        "com.spotify.music" to AppCategory.MULTIMEDIA,
        "com.maxmpz.audioplayer" to AppCategory.MULTIMEDIA,

        // Herramientas
        "com.notion.android" to AppCategory.HERRAMIENTAS,
        "md.obsidian" to AppCategory.HERRAMIENTAS,
        "com.todoist" to AppCategory.HERRAMIENTAS,
        "com.ticktick.task" to AppCategory.HERRAMIENTAS,
        "com.microsoft.todos" to AppCategory.HERRAMIENTAS,
        "com.google.android.keep" to AppCategory.HERRAMIENTAS,
        "com.google.android.apps.docs" to AppCategory.HERRAMIENTAS,
        "com.google.android.apps.sheets" to AppCategory.HERRAMIENTAS,
        "com.google.android.apps.slides" to AppCategory.HERRAMIENTAS,
        "com.google.android.apps.photos" to AppCategory.HERRAMIENTAS,
        "com.niksoftware.snapseed" to AppCategory.HERRAMIENTAS,
        "com.booking" to AppCategory.HERRAMIENTAS,
        "com.airbnb.android" to AppCategory.HERRAMIENTAS,
        "com.tripadvisor.tripadvisor" to AppCategory.HERRAMIENTAS,
        "com.google.android.play.games" to AppCategory.GAMES
    )

    private data class KeywordRule(
        val keyword: String,
        val category: String,
        val exactToken: Boolean = true,
        val negativeKeywords: List<String> = emptyList()
    )

    private val keywordRules = listOf(
        // Compras
        KeywordRule("binance", AppCategory.COMPRAS),
        KeywordRule("coinbase", AppCategory.COMPRAS),
        KeywordRule("yape", AppCategory.COMPRAS),
        KeywordRule("paypal", AppCategory.COMPRAS),
        KeywordRule("lemon", AppCategory.COMPRAS),
        KeywordRule("wise", AppCategory.COMPRAS),
        KeywordRule("mercadopago", AppCategory.COMPRAS),
        KeywordRule("crypto", AppCategory.COMPRAS),
        KeywordRule("wallet", AppCategory.COMPRAS, exactToken = false, negativeKeywords = listOf("wallpaper")),
        KeywordRule("blockchain", AppCategory.COMPRAS),
        KeywordRule("ledger", AppCategory.COMPRAS),
        KeywordRule("metamask", AppCategory.COMPRAS),
        KeywordRule("trust", AppCategory.COMPRAS, exactToken = false),
        KeywordRule("exodus", AppCategory.COMPRAS),
        KeywordRule("monero", AppCategory.COMPRAS),
        KeywordRule("nequi", AppCategory.COMPRAS),
        KeywordRule("daviplata", AppCategory.COMPRAS),
        KeywordRule("nubank", AppCategory.COMPRAS),
        KeywordRule("uala", AppCategory.COMPRAS),
        KeywordRule("plin", AppCategory.COMPRAS),
        KeywordRule("rappi", AppCategory.COMPRAS),
        KeywordRule("mercadolibre", AppCategory.COMPRAS),
        KeywordRule("amazon", AppCategory.COMPRAS, negativeKeywords = listOf("music", "video", "kindle")),
        KeywordRule("aliexpress", AppCategory.COMPRAS),
        KeywordRule("pedidosya", AppCategory.COMPRAS),
        KeywordRule("ubereats", AppCategory.COMPRAS),
        KeywordRule("doordash", AppCategory.COMPRAS),
        KeywordRule("grubhub", AppCategory.COMPRAS),
        KeywordRule("delivery", AppCategory.COMPRAS),
        KeywordRule("shop", AppCategory.COMPRAS, exactToken = false, negativeKeywords = listOf("workshop")),
        KeywordRule("store", AppCategory.COMPRAS, exactToken = false, negativeKeywords = listOf("restore", "play")),
        KeywordRule("shein", AppCategory.COMPRAS),
        KeywordRule("temu", AppCategory.COMPRAS),
        KeywordRule("ebay", AppCategory.COMPRAS),
        KeywordRule("wish", AppCategory.COMPRAS),
        KeywordRule("glovo", AppCategory.COMPRAS),
        KeywordRule("cornershop", AppCategory.COMPRAS),
        KeywordRule("ifood", AppCategory.COMPRAS),
        KeywordRule("vending", AppCategory.COMPRAS),

        // Multimedia
        KeywordRule("spotify", AppCategory.MULTIMEDIA),
        KeywordRule("deezer", AppCategory.MULTIMEDIA),
        KeywordRule("tidal", AppCategory.MULTIMEDIA),
        KeywordRule("podcast", AppCategory.MULTIMEDIA),
        KeywordRule("audio", AppCategory.MULTIMEDIA, exactToken = false, negativeKeywords = listOf("audiobook")),
        KeywordRule("soundcloud", AppCategory.MULTIMEDIA),
        KeywordRule("youtube", AppCategory.MULTIMEDIA),
        KeywordRule("netflix", AppCategory.MULTIMEDIA),
        KeywordRule("vlc", AppCategory.MULTIMEDIA),
        KeywordRule("tiktok", AppCategory.MULTIMEDIA),
        KeywordRule("disney", AppCategory.MULTIMEDIA),
        KeywordRule("hbo", AppCategory.MULTIMEDIA),
        KeywordRule("twitch", AppCategory.MULTIMEDIA),
        KeywordRule("crunchyroll", AppCategory.MULTIMEDIA),
        KeywordRule("plex", AppCategory.MULTIMEDIA),
        KeywordRule("video", AppCategory.MULTIMEDIA, exactToken = false, negativeKeywords = listOf("editor")),
        KeywordRule("music", AppCategory.MULTIMEDIA, exactToken = false),
        KeywordRule("player", AppCategory.MULTIMEDIA, exactToken = false),

        // Games
        KeywordRule("game", AppCategory.GAMES, exactToken = false),
        KeywordRule("games", AppCategory.GAMES, exactToken = false),
        KeywordRule("play", AppCategory.GAMES, exactToken = true, negativeKeywords = listOf("google", "display")),

        // Herramientas
        KeywordRule("browser", AppCategory.HERRAMIENTAS),
        KeywordRule("chrome", AppCategory.HERRAMIENTAS),
        KeywordRule("firefox", AppCategory.HERRAMIENTAS),
        KeywordRule("opera", AppCategory.HERRAMIENTAS),
        KeywordRule("edge", AppCategory.HERRAMIENTAS),
        KeywordRule("brave", AppCategory.HERRAMIENTAS),
        KeywordRule("notion", AppCategory.HERRAMIENTAS),
        KeywordRule("obsidian", AppCategory.HERRAMIENTAS),
        KeywordRule("todoist", AppCategory.HERRAMIENTAS),
        KeywordRule("ticktick", AppCategory.HERRAMIENTAS),
        KeywordRule("keep", AppCategory.HERRAMIENTAS),
        KeywordRule("excel", AppCategory.HERRAMIENTAS),
        KeywordRule("word", AppCategory.HERRAMIENTAS),
        KeywordRule("office", AppCategory.HERRAMIENTAS),
        KeywordRule("drive", AppCategory.HERRAMIENTAS),
        KeywordRule("docs", AppCategory.HERRAMIENTAS),
        KeywordRule("sheets", AppCategory.HERRAMIENTAS),
        KeywordRule("slides", AppCategory.HERRAMIENTAS),
        KeywordRule("calendar", AppCategory.HERRAMIENTAS),
        KeywordRule("notes", AppCategory.HERRAMIENTAS),
        KeywordRule("termux", AppCategory.HERRAMIENTAS),
        KeywordRule("fdroid", AppCategory.HERRAMIENTAS),
        KeywordRule("github", AppCategory.HERRAMIENTAS),
        KeywordRule("gitlab", AppCategory.HERRAMIENTAS),
        KeywordRule("acode", AppCategory.HERRAMIENTAS),
        KeywordRule("terminal", AppCategory.HERRAMIENTAS),
        KeywordRule("docker", AppCategory.HERRAMIENTAS),
        KeywordRule("git", AppCategory.HERRAMIENTAS, exactToken = true, negativeKeywords = listOf("digital")),
        KeywordRule("ide", AppCategory.HERRAMIENTAS),
        KeywordRule("code", AppCategory.HERRAMIENTAS, exactToken = true, negativeKeywords = listOf("qrcode", "barcode", "decode")),
        KeywordRule("dev", AppCategory.HERRAMIENTAS, exactToken = true, negativeKeywords = listOf("device", "devocional")),
        KeywordRule("obtainium", AppCategory.HERRAMIENTAS),
        KeywordRule("revanced", AppCategory.HERRAMIENTAS),
        KeywordRule("maps", AppCategory.HERRAMIENTAS),
        KeywordRule("gps", AppCategory.HERRAMIENTAS),
        KeywordRule("uber", AppCategory.HERRAMIENTAS, negativeKeywords = listOf("ubereats")),
        KeywordRule("lyft", AppCategory.HERRAMIENTAS),
        KeywordRule("didi", AppCategory.HERRAMIENTAS),
        KeywordRule("cabify", AppCategory.HERRAMIENTAS),
        KeywordRule("indriver", AppCategory.HERRAMIENTAS),
        KeywordRule("waze", AppCategory.HERRAMIENTAS),
        KeywordRule("transit", AppCategory.HERRAMIENTAS),
        KeywordRule("booking", AppCategory.HERRAMIENTAS),
        KeywordRule("airbnb", AppCategory.HERRAMIENTAS),
        KeywordRule("flight", AppCategory.HERRAMIENTAS),
        KeywordRule("airline", AppCategory.HERRAMIENTAS),
        KeywordRule("settings", AppCategory.HERRAMIENTAS),
        KeywordRule("monitor", AppCategory.HERRAMIENTAS),
        KeywordRule("filemanager", AppCategory.HERRAMIENTAS),
        KeywordRule("files", AppCategory.HERRAMIENTAS),
        KeywordRule("manager", AppCategory.HERRAMIENTAS, exactToken = true, negativeKeywords = listOf("password")),
        KeywordRule("updater", AppCategory.HERRAMIENTAS),
        KeywordRule("calculator", AppCategory.HERRAMIENTAS),
        KeywordRule("calc", AppCategory.HERRAMIENTAS, exactToken = false),
        KeywordRule("clock", AppCategory.HERRAMIENTAS),
        KeywordRule("alarm", AppCategory.HERRAMIENTAS),
        KeywordRule("weather", AppCategory.HERRAMIENTAS),
        KeywordRule("translate", AppCategory.HERRAMIENTAS),
        KeywordRule("flashlight", AppCategory.HERRAMIENTAS),
        KeywordRule("recorder", AppCategory.HERRAMIENTAS),
        KeywordRule("vpn", AppCategory.HERRAMIENTAS),
        KeywordRule("password", AppCategory.HERRAMIENTAS),
        KeywordRule("authenticator", AppCategory.HERRAMIENTAS),
        KeywordRule("backup", AppCategory.HERRAMIENTAS),
        KeywordRule("cleaner", AppCategory.HERRAMIENTAS),
        KeywordRule("database", AppCategory.HERRAMIENTAS),
        KeywordRule("sql", AppCategory.HERRAMIENTAS),
        KeywordRule("gallery", AppCategory.HERRAMIENTAS),
        KeywordRule("photo", AppCategory.HERRAMIENTAS, exactToken = false, negativeKeywords = listOf("photon")),
        KeywordRule("photos", AppCategory.HERRAMIENTAS),
        KeywordRule("camera", AppCategory.HERRAMIENTAS),
        KeywordRule("draw", AppCategory.HERRAMIENTAS),
        KeywordRule("paint", AppCategory.HERRAMIENTAS),
        KeywordRule("image", AppCategory.HERRAMIENTAS),
        KeywordRule("sketch", AppCategory.HERRAMIENTAS),
        KeywordRule("canva", AppCategory.HERRAMIENTAS),
        KeywordRule("snapseed", AppCategory.HERRAMIENTAS),
        KeywordRule("lightroom", AppCategory.HERRAMIENTAS),
        KeywordRule("figma", AppCategory.HERRAMIENTAS),
        KeywordRule("fitness", AppCategory.HERRAMIENTAS),
        KeywordRule("workout", AppCategory.HERRAMIENTAS),
        KeywordRule("health", AppCategory.HERRAMIENTAS),
        KeywordRule("meditation", AppCategory.HERRAMIENTAS),
        KeywordRule("strava", AppCategory.HERRAMIENTAS),
        KeywordRule("fasting", AppCategory.HERRAMIENTAS),
        KeywordRule("mail", AppCategory.HERRAMIENTAS),
        KeywordRule("email", AppCategory.HERRAMIENTAS),
        KeywordRule("gmail", AppCategory.HERRAMIENTAS),
        KeywordRule("slack", AppCategory.HERRAMIENTAS),
        KeywordRule("trello", AppCategory.HERRAMIENTAS),
        KeywordRule("asana", AppCategory.HERRAMIENTAS),
        KeywordRule("linear", AppCategory.HERRAMIENTAS)
    )

    fun categorize(packageName: String, androidCategory: Int, isSystemApp: Boolean = false): String {
        if (isSystemApp) return AppCategory.SYSTEM

        val lowerPkg = packageName.lowercase()
        exactPackageCategories[lowerPkg]?.let { return it }

        // Tokenize package name
        val tokens = lowerPkg.split('.', '_', '-', '/')

        for (rule in keywordRules) {
            val negativeMatch = rule.negativeKeywords.any { neg -> lowerPkg.contains(neg) }
            if (negativeMatch) continue

            val isMatch = if (rule.exactToken) {
                tokens.any { it == rule.keyword }
            } else {
                tokens.any { it.contains(rule.keyword) }
            }

            if (isMatch) {
                return rule.category
            }
        }

        // Fall back to Android's built-in category mapping cleanly to our 5 target categories
        return when (androidCategory) {
            android.content.pm.ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            android.content.pm.ApplicationInfo.CATEGORY_AUDIO -> AppCategory.MULTIMEDIA
            android.content.pm.ApplicationInfo.CATEGORY_VIDEO -> AppCategory.MULTIMEDIA
            android.content.pm.ApplicationInfo.CATEGORY_MAPS -> AppCategory.HERRAMIENTAS
            android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.HERRAMIENTAS
            else -> AppCategory.ALL
        }
    }
}
