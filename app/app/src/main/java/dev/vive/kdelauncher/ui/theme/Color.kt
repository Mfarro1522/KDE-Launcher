package dev.vive.kdelauncher.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for dark and light themes.
 * Dark: deep blacks with teal/orange accents.
 * Light: warm off-whites with deeper teal/orange accents.
 */
object LauncherColors {

    // ── Dark Theme ──
    val DarkBackground = Color(0xFF0F0F0F)
    val DarkSurface = Color(0xFF1A1A1A)
    val DarkSurfaceVariant = Color(0xFF252525)
    val DarkOnBackground = Color(0xFFF0F0F0)
    val DarkOnSurface = Color(0xFFF0F0F0)
    val DarkOnSurfaceVariant = Color(0xFF8A8A8A)
    val DarkBorder = Color(0xFF2E2E2E)

    // ── Light Theme ──
    val LightBackground = Color(0xFFF8F7F4)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFEFEDE8)
    val LightOnBackground = Color(0xFF1A1A1A)
    val LightOnSurface = Color(0xFF1A1A1A)
    val LightOnSurfaceVariant = Color(0xFF6B6B6B)
    val LightBorder = Color(0xFFE0DDD6)

    // ── Accent - Personal (Teal) ──
    val AccentTeal = Color(0xFF00BFA5)
    val AccentTealDark = Color(0xFF00897B)
    val AccentTealBg = Color(0x2600BFA5)
    val AccentTealBgLight = Color(0x1A00BFA5)

    // ── Accent - Work (Orange) ──
    val AccentOrange = Color(0xFFFF9100)
    val AccentOrangeDark = Color(0xFFF57C00)
    val AccentOrangeBg = Color(0x26FF9100)
    val AccentOrangeBgLight = Color(0x1AFF9100)

    // ── Status ──
    val StatusOnline = Color(0xFF4CAF50)
    val StatusWork = Color(0xFFFF9100)

    // ── Utility ──
    val White5 = Color(0x0DFFFFFF)
    val White10 = Color(0x1AFFFFFF)
    val Black5 = Color(0x0D000000)
    val Black10 = Color(0x1A000000)
}

/**
 * Resolved colors based on current theme (dark/light).
 * Use this instead of accessing LauncherColors directly in components.
 */
data class ResolvedColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val border: Color,
    val isDark: Boolean,
) {
    val borderSubtle: Color get() = if (isDark) LauncherColors.White5 else LauncherColors.Black5
    val overlay: Color get() = if (isDark) LauncherColors.White5 else LauncherColors.Black5

    companion object {
        val Dark = ResolvedColors(
            background = LauncherColors.DarkBackground,
            surface = LauncherColors.DarkSurface,
            surfaceVariant = LauncherColors.DarkSurfaceVariant,
            onBackground = LauncherColors.DarkOnBackground,
            onSurface = LauncherColors.DarkOnSurface,
            onSurfaceVariant = LauncherColors.DarkOnSurfaceVariant,
            border = LauncherColors.DarkBorder,
            isDark = true,
        )
        val Light = ResolvedColors(
            background = LauncherColors.LightBackground,
            surface = LauncherColors.LightSurface,
            surfaceVariant = LauncherColors.LightSurfaceVariant,
            onBackground = LauncherColors.LightOnBackground,
            onSurface = LauncherColors.LightOnSurface,
            onSurfaceVariant = LauncherColors.LightOnSurfaceVariant,
            border = LauncherColors.LightBorder,
            isDark = false,
        )
    }
}
