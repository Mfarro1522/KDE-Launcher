package dev.vive.kdelauncher.data.model

enum class ColorTheme(
    val displayName: String,
    private val darkBackgroundArgbValue: Long,
    private val darkSurfaceArgbValue: Long,
    private val darkAccentArgbValue: Long,
    private val darkOnBackgroundArgbValue: Long,
    private val lightBackgroundArgbValue: Long,
    private val lightSurfaceArgbValue: Long,
    private val lightAccentArgbValue: Long,
    private val lightOnBackgroundArgbValue: Long,
) {
    SYSTEM(
        displayName = "System",
        darkBackgroundArgbValue = 0xFF0F0F0FL,
        darkSurfaceArgbValue = 0xFF1A1A1AL,
        darkAccentArgbValue = 0xFF00BFA5L,
        darkOnBackgroundArgbValue = 0xFFF0F0F0L,
        lightBackgroundArgbValue = 0xFFF8F7F4L,
        lightSurfaceArgbValue = 0xFFFFFFFFL,
        lightAccentArgbValue = 0xFF00897BL,
        lightOnBackgroundArgbValue = 0xFF1A1A1AL,
    ),
    DRACULA(
        displayName = "Dracula",
        darkBackgroundArgbValue = 0xFF282A36L,
        darkSurfaceArgbValue = 0xFF44475AL,
        darkAccentArgbValue = 0xFFBD93F9L,
        darkOnBackgroundArgbValue = 0xFFF8F8F2L,
        lightBackgroundArgbValue = 0xFFF6F2FFL,
        lightSurfaceArgbValue = 0xFFFFFFFFL,
        lightAccentArgbValue = 0xFF8B5CF6L,
        lightOnBackgroundArgbValue = 0xFF2B2238L,
    ),
    TOKYO_NIGHT(
        displayName = "Tokyo Night",
        darkBackgroundArgbValue = 0xFF1A1B2EL,
        darkSurfaceArgbValue = 0xFF202330L,
        darkAccentArgbValue = 0xFF7AA2F7L,
        darkOnBackgroundArgbValue = 0xFFC0CAF5L,
        lightBackgroundArgbValue = 0xFFF3F6FFL,
        lightSurfaceArgbValue = 0xFFFFFFFFL,
        lightAccentArgbValue = 0xFF4C6FBFL,
        lightOnBackgroundArgbValue = 0xFF1F2335L,
    ),
    VERCEL(
        displayName = "Vercel",
        darkBackgroundArgbValue = 0xFF111111L,
        darkSurfaceArgbValue = 0xFF1A1A1AL,
        darkAccentArgbValue = 0xFFFFFFFFL,
        darkOnBackgroundArgbValue = 0xFFF5F5F5L,
        lightBackgroundArgbValue = 0xFFFFFFFFL,
        lightSurfaceArgbValue = 0xFFF6F6F6L,
        lightAccentArgbValue = 0xFF111111L,
        lightOnBackgroundArgbValue = 0xFF111111L,
    ),
    CATPPUCCIN(
        displayName = "Catppuccin Mocha",
        darkBackgroundArgbValue = 0xFF1E1E2EL,
        darkSurfaceArgbValue = 0xFF313244L,
        darkAccentArgbValue = 0xFFCBA6F7L,
        darkOnBackgroundArgbValue = 0xFFCDD6F4L,
        lightBackgroundArgbValue = 0xFFEDEFF5L,
        lightSurfaceArgbValue = 0xFFFFFFFFL,
        lightAccentArgbValue = 0xFF8839EAL,
        lightOnBackgroundArgbValue = 0xFF4C4F69L,
    ),
    NORD(
        displayName = "Nord",
        darkBackgroundArgbValue = 0xFF2E3440L,
        darkSurfaceArgbValue = 0xFF3B4252L,
        darkAccentArgbValue = 0xFF88C0D0L,
        darkOnBackgroundArgbValue = 0xFFECEFF4L,
        lightBackgroundArgbValue = 0xFFECEFF4L,
        lightSurfaceArgbValue = 0xFFFFFFFFL,
        lightAccentArgbValue = 0xFF5E81ACL,
        lightOnBackgroundArgbValue = 0xFF2E3440L,
    ),
    GRUVBOX(
        displayName = "Gruvbox",
        darkBackgroundArgbValue = 0xFF282828L,
        darkSurfaceArgbValue = 0xFF3C3836L,
        darkAccentArgbValue = 0xFFB8BB26L,
        darkOnBackgroundArgbValue = 0xFFEBDBB2L,
        lightBackgroundArgbValue = 0xFFFBF1C7L,
        lightSurfaceArgbValue = 0xFFF2E5BCL,
        lightAccentArgbValue = 0xFF98971AL,
        lightOnBackgroundArgbValue = 0xFF3C3836L,
    ),
    ONE_DARK(
        displayName = "One Dark",
        darkBackgroundArgbValue = 0xFF282C34L,
        darkSurfaceArgbValue = 0xFF21252BL,
        darkAccentArgbValue = 0xFF61AFEFL,
        darkOnBackgroundArgbValue = 0xFFABB2BFL,
        lightBackgroundArgbValue = 0xFFF7F7F8L,
        lightSurfaceArgbValue = 0xFFFFFFFFL,
        lightAccentArgbValue = 0xFF4078F2L,
        lightOnBackgroundArgbValue = 0xFF383A42L,
    );

    val backgroundArgb: Long get() = darkBackgroundArgbValue
    val surfaceArgb: Long get() = darkSurfaceArgbValue
    val accentArgb: Long get() = darkAccentArgbValue
    val onBackgroundArgb: Long get() = darkOnBackgroundArgbValue

    fun backgroundArgb(isDark: Boolean): Long =
        if (isDark) darkBackgroundArgbValue else lightBackgroundArgbValue

    fun surfaceArgb(isDark: Boolean): Long =
        if (isDark) darkSurfaceArgbValue else lightSurfaceArgbValue

    fun accentArgb(isDark: Boolean): Long =
        if (isDark) darkAccentArgbValue else lightAccentArgbValue

    fun onBackgroundArgb(isDark: Boolean): Long =
        if (isDark) darkOnBackgroundArgbValue else lightOnBackgroundArgbValue

    companion object {
        fun fromName(name: String?): ColorTheme {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
        }
    }
}
