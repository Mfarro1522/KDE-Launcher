package dev.vive.kdelauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.vive.kdelauncher.data.model.ColorTheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.data.model.ProfileType

/**
 * Composition local for accent colors (profile-dependent).
 */
data class LauncherAccent(
    val primary: Color = LauncherColors.AccentTeal,
    val primaryDark: Color = LauncherColors.AccentTealDark,
    val primaryBg: Color = LauncherColors.AccentTealBg,
    val primaryBgLight: Color = LauncherColors.AccentTealBgLight,
)

val LocalLauncherAccent = compositionLocalOf { LauncherAccent() }

/**
 * Composition local for resolved theme colors (dark/light aware).
 */
val LocalColors = compositionLocalOf { ResolvedColors.Dark }

fun accentFromProfile(profile: Profile): LauncherAccent {
    return when (profile.type) {
        ProfileType.PERSONAL -> LauncherAccent(
            primary = LauncherColors.AccentTeal,
            primaryDark = LauncherColors.AccentTealDark,
            primaryBg = LauncherColors.AccentTealBg,
            primaryBgLight = LauncherColors.AccentTealBgLight,
        )
        ProfileType.WORK -> LauncherAccent(
            primary = LauncherColors.AccentOrange,
            primaryDark = LauncherColors.AccentOrangeDark,
            primaryBg = LauncherColors.AccentOrangeBg,
            primaryBgLight = LauncherColors.AccentOrangeBgLight,
        )
    }
}

@Composable
fun KDELauncherTheme(
    profile: Profile = Profile.Personal,
    isDarkTheme: Boolean = true,
    colorTheme: ColorTheme = ColorTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val resolvedColors = ResolvedColors.resolveFromTheme(colorTheme, isDarkTheme)
    
    val baseAccent = accentFromProfile(profile)
    val accent = if (colorTheme == ColorTheme.SYSTEM) baseAccent else LauncherAccent(
        primary = resolvedColors.accent,
        primaryDark = resolvedColors.accent.copy(alpha = 0.8f),
        primaryBg = resolvedColors.accent.copy(alpha = 0.15f),
        primaryBgLight = resolvedColors.accent.copy(alpha = 0.1f),
    )

    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = accent.primary,
            onPrimary = resolvedColors.background,
            secondary = resolvedColors.surfaceVariant,
            onSecondary = resolvedColors.onSurface,
            tertiary = accent.primaryDark,
            background = resolvedColors.background,
            onBackground = resolvedColors.onBackground,
            surface = resolvedColors.surface,
            onSurface = resolvedColors.onSurface,
            surfaceVariant = resolvedColors.surfaceVariant,
            onSurfaceVariant = resolvedColors.onSurfaceVariant,
            outline = resolvedColors.border,
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            onPrimary = resolvedColors.background,
            secondary = resolvedColors.surfaceVariant,
            onSecondary = resolvedColors.onSurface,
            tertiary = accent.primaryDark,
            background = resolvedColors.background,
            onBackground = resolvedColors.onBackground,
            surface = resolvedColors.surface,
            onSurface = resolvedColors.onSurface,
            surfaceVariant = resolvedColors.surfaceVariant,
            onSurfaceVariant = resolvedColors.onSurfaceVariant,
            outline = resolvedColors.border,
        )
    }

    CompositionLocalProvider(
        LocalLauncherAccent provides accent,
        LocalColors provides resolvedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LauncherTypography,
            content = content
        )
    }
}
