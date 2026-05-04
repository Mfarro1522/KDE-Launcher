package dev.vive.kdelauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography scale for the launcher.
 * Uses system default sans-serif (Roboto on most devices)
 * and monospace for technical/numeric elements.
 */
val LauncherFontFamily = FontFamily.SansSerif
val MonoFontFamily = FontFamily.Monospace

val LauncherTypography = Typography(
    // Large display text (clock, etc.)
    displayLarge = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 72.sp,
        letterSpacing = (-2).sp,
    ),

    // Section titles
    titleLarge = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = (-0.5).sp,
    ),

    // Profile name, category labels
    titleMedium = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = (-0.3).sp,
    ),

    // Subtitle / secondary text
    titleSmall = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),

    // Body text
    bodyLarge = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),

    // App labels
    bodySmall = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
    ),

    // Category count, small badges
    labelSmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
    ),

    // Quick settings labels
    labelMedium = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
    ),

    // Mono text for status bar time
    labelLarge = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = (-0.5).sp,
    ),
)
