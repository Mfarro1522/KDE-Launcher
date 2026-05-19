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
 * The AI can also create dynamic categories on the fly.
 */
object AppCategory {
    const val FAVORITES = "favorites"
    const val ALL = "all"
    const val SOCIAL = "social"
    const val PRODUCTIVITY = "productivity"
    const val UTILITIES = "utilities"
    const val SYSTEM = "system"
    const val GAMES = "games"
    const val MUSIC = "music"
    const val STREAMING = "streaming"
    const val MULTIMEDIA = "multimedia"
    const val WALLETS = "wallets"
    const val COMPRAS = "compras"
    const val FINANZAS = "finanzas"
    const val DEV = "dev"

    /** Categories always visible in settings / tabs */
    val FIXED = listOf(FAVORITES, ALL, SOCIAL, PRODUCTIVITY, UTILITIES)

    /** Categories handled locally and excluded from AI payload. */
    val AI_EXCLUDED = setOf(SYSTEM, GAMES, MUSIC, STREAMING, MULTIMEDIA, WALLETS, COMPRAS, FINANZAS, DEV)

    /** Human-readable labels (Spanish). Dynamic categories fallback to capitalized ID. */
    fun displayName(id: String): String = when (id) {
        FAVORITES -> "Favoritos"
        ALL -> "Todas"
        SOCIAL -> "Social"
        PRODUCTIVITY -> "Productividad"
        UTILITIES -> "Utilidades"
        SYSTEM -> "Apps del Sistema"
        GAMES -> "Juegos"
        MUSIC -> "Música"
        STREAMING -> "Streaming"
        MULTIMEDIA -> "Multimedia"
        WALLETS -> "Wallets"
        COMPRAS -> "Compras"
        FINANZAS -> "Finanzas"
        DEV -> "Dev"
        "media" -> "Media"
        "creativity" -> "Creatividad"
        "finance" -> "Finanzas"
        "shopping" -> "Compras"
        "travel" -> "Viajes"
        "browsers" -> "Navegadores"
        "development" -> "Desarrollo"
        else -> id.replaceFirstChar { it.uppercase() }
    }

    /** Default Material icon name for a category ID. */
    fun defaultIcon(id: String): String = when (id) {
        FAVORITES -> "Star"
        ALL -> "GridView"
        SOCIAL -> "Forum"
        PRODUCTIVITY -> "Work"
        UTILITIES -> "Build"
        SYSTEM -> "Settings"
        GAMES -> "Gamepad"
        MUSIC -> "Headphones"
        STREAMING -> "Cloud"
        MULTIMEDIA -> "Headphones"
        WALLETS -> "Diamond"
        COMPRAS -> "ShoppingCart"
        FINANZAS -> "AttachMoney"
        DEV -> "Terminal"
        "media" -> "PlayArrow"
        "creativity" -> "Palette"
        "finance" -> "AttachMoney"
        "shopping" -> "ShoppingCart"
        "travel" -> "Map"
        "browsers" -> "Language"
        "development" -> "Code"
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
}

/**
 * Categorization rules based on Android's ApplicationInfo.category
 * and common package name patterns.
 * Returns a String category ID so the AI / heuristics can use any taxonomy.
 */
object AppCategorizer {

    /**
     * Enhanced categorization rule with optional semantic refinements:
     * - [bigramContext]: bonus +15 when a second token co-occurs (e.g. "music" + "player")
     * - [negativePatterns]: penalty -30 when false-positive tokens are present
     *   (e.g. "play" should NOT match "google.android.play" → Play Store)
     */
    private data class CategoryRule(
        val pattern: String,
        val category: String,
        val baseScore: Int = 10,
        val bigramContext: String? = null,
        val negativePatterns: List<String>? = null
    )

    private val exactPackageCategories = mapOf(
        // Prevent misclassification of well-known ambiguous packages
        "com.android.vending" to "shopping",
        "com.facebook.pages.app" to "social",
        "com.google.android.apps.photos" to "creativity",
        "com.google.android.youtube" to AppCategory.STREAMING,
        "com.google.android.play.games" to AppCategory.GAMES,
        // Productivity — exact matches for apps whose packageName is misleading
        "com.notion.android" to "productivity",
        "md.obsidian" to "productivity",
        "com.todoist" to "productivity",
        "com.ticktick.task" to "productivity",
        "com.microsoft.todos" to "productivity",
        "com.google.android.keep" to "productivity",
        // Health / Fitness
        "com.google.android.apps.fitness" to "health",
        "com.strava" to "health",
        "com.myfitnesspal.android" to "health",
        // Travel — exact
        "com.booking" to "travel",
        "com.airbnb.android" to "travel",
        "com.tripadvisor.tripadvisor" to "travel",
        // LATAM fintech
        "com.bcp.innovacxion.yapeapp" to AppCategory.WALLETS,
        "pe.interbank.mobilebanking" to AppCategory.WALLETS,
        "com.bbva.nxt_peru" to AppCategory.WALLETS,
        "ar.com.uala" to AppCategory.WALLETS,
        "com.nubank" to AppCategory.WALLETS,
        "com.mercadopago.wallet" to AppCategory.WALLETS,
        // LATAM delivery
        "com.rappi" to AppCategory.COMPRAS,
        "com.pedidosya" to AppCategory.COMPRAS,
        "com.cornershopapp.shopper" to AppCategory.COMPRAS
    )

    private val categoryRules = listOf(
        // ── Wallets / Crypto ───────────────────────────────
        CategoryRule("binance", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("coinbase", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("yape", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("paypal", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("lemon", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("wise", AppCategory.WALLETS, baseScore = 20, negativePatterns = listOf("otherwise")),
        CategoryRule("mercadopago", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("crypto", AppCategory.WALLETS, baseScore = 16),
        CategoryRule("wallet", AppCategory.WALLETS, baseScore = 16, negativePatterns = listOf("wallpaper")),
        CategoryRule("blockchain", AppCategory.WALLETS, baseScore = 16),
        CategoryRule("ledger", AppCategory.WALLETS, baseScore = 16),
        CategoryRule("metamask", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("trust", AppCategory.WALLETS, baseScore = 16, bigramContext = "wallet"),
        CategoryRule("exodus", AppCategory.WALLETS, baseScore = 16),
        CategoryRule("monero", AppCategory.WALLETS, baseScore = 16),
        CategoryRule("nequi", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("daviplata", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("nubank", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("uala", AppCategory.WALLETS, baseScore = 20),
        CategoryRule("plin", AppCategory.WALLETS, baseScore = 20),

        // ── Compras / E-commerce / Delivery ────────────────
        CategoryRule("rappi", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("mercadolibre", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("amazon", AppCategory.COMPRAS, baseScore = 20, negativePatterns = listOf("music", "video", "kindle")),
        CategoryRule("aliexpress", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("pedidosya", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("ubereats", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("doordash", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("grubhub", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("delivery", AppCategory.COMPRAS, baseScore = 14),
        CategoryRule("shop", AppCategory.COMPRAS, baseScore = 12, negativePatterns = listOf("workshop")),
        CategoryRule("store", AppCategory.COMPRAS, baseScore = 10, negativePatterns = listOf("neo.store", "restore")),
        CategoryRule("shein", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("temu", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("ebay", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("wish", AppCategory.COMPRAS, baseScore = 16),
        CategoryRule("glovo", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("cornershop", AppCategory.COMPRAS, baseScore = 20),
        CategoryRule("ifood", AppCategory.COMPRAS, baseScore = 20),

        // ── Dev (heavy dev tools) ──────────────────────────
        CategoryRule("termux", AppCategory.DEV, baseScore = 20),
        CategoryRule("fdroid", AppCategory.DEV, baseScore = 20),
        CategoryRule("github", AppCategory.DEV, baseScore = 20),
        CategoryRule("gitlab", AppCategory.DEV, baseScore = 20),
        CategoryRule("acode", AppCategory.DEV, baseScore = 20),
        CategoryRule("neo.store", AppCategory.DEV, baseScore = 20),
        CategoryRule("terminal", AppCategory.DEV, baseScore = 16),
        CategoryRule("docker", AppCategory.DEV, baseScore = 16),
        CategoryRule("git", AppCategory.DEV, baseScore = 16, negativePatterns = listOf("digital")),
        CategoryRule("editor", AppCategory.DEV, baseScore = 12, bigramContext = "code"),
        CategoryRule("ide", AppCategory.DEV, baseScore = 14),
        CategoryRule("code", AppCategory.DEV, baseScore = 12, negativePatterns = listOf("qrcode", "barcode", "decode")),
        CategoryRule("dev", AppCategory.DEV, baseScore = 10, negativePatterns = listOf("device", "devocional")),
        CategoryRule("obtainium", AppCategory.DEV, baseScore = 20),
        CategoryRule("revanced", AppCategory.DEV, baseScore = 16),

        // ── Development (general) ──────────────────────────
        CategoryRule("database", "development"),
        CategoryRule("sql", "development"),

        // ── Graphics / Creativity ──────────────────────────
        CategoryRule("gallery", "creativity"),
        CategoryRule("photo", "creativity", negativePatterns = listOf("photon")),
        CategoryRule("photos", "creativity", baseScore = 14),
        CategoryRule("camera", "creativity"),
        CategoryRule("draw", "creativity"),
        CategoryRule("paint", "creativity"),
        CategoryRule("image", "creativity", bigramContext = "editor"),
        CategoryRule("sketch", "creativity"),
        CategoryRule("canva", "creativity"),
        CategoryRule("snapseed", "creativity", baseScore = 16),
        CategoryRule("lightroom", "creativity", baseScore = 16),
        CategoryRule("figma", "creativity", baseScore = 16),

        // ── Browsers ───────────────────────────────────────
        CategoryRule("browser", "browsers"),
        CategoryRule("chrome", "browsers"),
        CategoryRule("firefox", "browsers"),
        CategoryRule("brave", "browsers", bigramContext = "browser"),

        // ── Social / Internet ──────────────────────────────
        CategoryRule("mail", "productivity", negativePatterns = listOf("mailspring")),
        CategoryRule("email", "productivity"),
        CategoryRule("gmail", "productivity"),
        CategoryRule("slack", "productivity"),
        CategoryRule("notion", "productivity"),
        CategoryRule("trello", "productivity"),
        CategoryRule("asana", "productivity"),
        CategoryRule("linear", "productivity"),
        CategoryRule("todoist", "productivity"),
        CategoryRule("telegram", "social"),
        CategoryRule("whatsapp", "social"),
        CategoryRule("discord", "social"),
        CategoryRule("twitter", "social"),
        CategoryRule("reddit", "social"),
        CategoryRule("instagram", "social"),
        CategoryRule("messenger", "social", negativePatterns = listOf("sms")),
        CategoryRule("signal", "social", bigramContext = "messenger"),
        CategoryRule("facebook", "social"),
        CategoryRule("pages", "social"),
        CategoryRule("threads", "social", baseScore = 14),
        CategoryRule("mastodon", "social", baseScore = 16),
        CategoryRule("bluesky", "social", baseScore = 16),
        CategoryRule("linkedin", "social", baseScore = 14),

        // ── Shopping ───────────────────────────────────────
        CategoryRule("store", "shopping", negativePatterns = listOf("neo.store", "restore")),
        CategoryRule("vending", "shopping", baseScore = 14),

        // ── Games ──────────────────────────────────────────
        CategoryRule("game", "games", baseScore = 14),
        CategoryRule("games", "games", baseScore = 16),
        CategoryRule("play", "games", baseScore = 5, negativePatterns = listOf("google", "display", "player")),

        // ── Music ──────────────────────────────────────────
        CategoryRule("music", AppCategory.MUSIC, baseScore = 14, bigramContext = "player"),
        CategoryRule("spotify", AppCategory.MUSIC, baseScore = 16),
        CategoryRule("deezer", AppCategory.MUSIC, baseScore = 16),
        CategoryRule("tidal", AppCategory.MUSIC, baseScore = 16),
        CategoryRule("apple.music", AppCategory.MUSIC, baseScore = 16),
        CategoryRule("amazon.music", AppCategory.MUSIC, baseScore = 16),
        CategoryRule("podcast", AppCategory.MUSIC, baseScore = 14),
        CategoryRule("audio", AppCategory.MUSIC, baseScore = 14, negativePatterns = listOf("audiobook")),
        CategoryRule("soundcloud", AppCategory.MUSIC, baseScore = 16),
        CategoryRule("youtube.music", AppCategory.MUSIC, baseScore = 20),

        // ── Streaming / Video ──────────────────────────────
        CategoryRule("video", AppCategory.STREAMING, baseScore = 14, negativePatterns = listOf("editor")),
        CategoryRule("youtube", AppCategory.STREAMING, baseScore = 16, negativePatterns = listOf("music")),
        CategoryRule("vlc", AppCategory.STREAMING, baseScore = 16),
        CategoryRule("netflix", AppCategory.STREAMING, baseScore = 16),
        CategoryRule("tiktok", AppCategory.STREAMING, baseScore = 14),
        CategoryRule("prime.video", AppCategory.STREAMING, baseScore = 16),
        CategoryRule("disney", AppCategory.STREAMING, baseScore = 16),
        CategoryRule("hbo", AppCategory.STREAMING, baseScore = 16),
        CategoryRule("twitch", AppCategory.STREAMING, baseScore = 16),
        CategoryRule("crunchyroll", AppCategory.STREAMING, baseScore = 16),
        CategoryRule("plex", AppCategory.STREAMING, baseScore = 16),

        // ── Health / Fitness ───────────────────────────────
        CategoryRule("fitness", "health", baseScore = 14),
        CategoryRule("workout", "health", baseScore = 14),
        CategoryRule("health", "health", baseScore = 12),
        CategoryRule("meditation", "health", baseScore = 14),
        CategoryRule("strava", "health", baseScore = 16),
        CategoryRule("fasting", "health", baseScore = 14),

        // ── System / Utilities ─────────────────────────────
        CategoryRule("settings", "utilities", baseScore = 18),
        CategoryRule("monitor", "utilities"),
        CategoryRule("filemanager", "utilities"),
        CategoryRule("files", "utilities"),
        CategoryRule("manager", "utilities", baseScore = 8, negativePatterns = listOf("password")),
        CategoryRule("updater", "utilities"),
        CategoryRule("calculator", "utilities", baseScore = 14),
        CategoryRule("calc", "utilities"),
        CategoryRule("clock", "utilities"),
        CategoryRule("alarm", "utilities"),
        CategoryRule("calendar", "productivity"),
        CategoryRule("notes", "productivity"),
        CategoryRule("weather", "utilities"),
        CategoryRule("maps", "travel", baseScore = 14),
        CategoryRule("translate", "utilities"),
        CategoryRule("compass", "travel"),
        CategoryRule("flashlight", "utilities"),
        CategoryRule("recorder", "utilities"),
        CategoryRule("contacts", "social"),
        CategoryRule("phone", "social", negativePatterns = listOf("headphone")),
        CategoryRule("dialer", "social"),
        CategoryRule("messages", "social"),
        CategoryRule("sms", "social"),
        CategoryRule("vpn", "utilities", baseScore = 14),
        CategoryRule("password", "utilities", baseScore = 14),
        CategoryRule("authenticator", "utilities", baseScore = 16),
        CategoryRule("backup", "utilities", baseScore = 12),
        CategoryRule("cleaner", "utilities", baseScore = 12),

        // ── Travel ─────────────────────────────────────────
        CategoryRule("uber", "travel", baseScore = 16, negativePatterns = listOf("ubereats")),
        CategoryRule("lyft", "travel", baseScore = 16),
        CategoryRule("booking", "travel", baseScore = 16),
        CategoryRule("airbnb", "travel", baseScore = 16),
        CategoryRule("flight", "travel", baseScore = 14),
        CategoryRule("airline", "travel", baseScore = 14),
        CategoryRule("didi", "travel", baseScore = 16),
        CategoryRule("cabify", "travel", baseScore = 16),
        CategoryRule("indriver", "travel", baseScore = 16)
    )

    /**
     * Categorize an app based on its package name, Android category info,
     * and whether it is a system app.
     */
    fun categorize(packageName: String, androidCategory: Int, isSystemApp: Boolean = false): String {
        if (isSystemApp) return AppCategory.SYSTEM

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
            android.content.pm.ApplicationInfo.CATEGORY_AUDIO -> AppCategory.MUSIC
            android.content.pm.ApplicationInfo.CATEGORY_VIDEO -> AppCategory.STREAMING
            android.content.pm.ApplicationInfo.CATEGORY_IMAGE -> "creativity"
            android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            android.content.pm.ApplicationInfo.CATEGORY_NEWS -> "browsers"
            android.content.pm.ApplicationInfo.CATEGORY_MAPS -> "travel"
            android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            else -> AppCategory.ALL
        }
    }

    /**
     * Score a rule against a package name with three refinements:
     * 1. Exact token match bonus (+20) — "music" in "com.app.music" vs substring in "communist"
     * 2. Bi-gram context bonus (+15) — "trust" scores higher when "wallet" co-occurs
     * 3. Negative pattern penalty (-30) — "play" is suppressed when "google" co-occurs
     */
    private fun scoreRule(packageName: String, rule: CategoryRule): Int? {
        if (!packageName.contains(rule.pattern)) return null

        // Negative patterns: if any match, strongly penalize or suppress
        val negativePenalty = rule.negativePatterns?.let { negatives ->
            if (negatives.any { packageName.contains(it) }) -30 else 0
        } ?: 0

        // Bi-gram context: bonus when a related token co-occurs
        val bigramBonus = rule.bigramContext?.let { ctx ->
            if (packageName.contains(ctx)) 15 else 0
        } ?: 0

        val exactTokenMatch = packageName
            .split('.', '_', '-', '/')
            .any { token -> token == rule.pattern }

        val exactTokenBonus = if (exactTokenMatch) 20 else 0
        val substringBonus = rule.pattern.length

        val total = rule.baseScore + exactTokenBonus + substringBonus + bigramBonus + negativePenalty
        return if (total > 0) total else null
    }
}
