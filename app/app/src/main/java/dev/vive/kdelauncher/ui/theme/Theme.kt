package dev.vive.kdelauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    content: @Composable () -> Unit
) {
    val accent = accentFromProfile(profile)
    val resolvedColors = if (isDarkTheme) ResolvedColors.Dark else ResolvedColors.Light

    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = accent.primary,
            onPrimary = LauncherColors.DarkBackground,
            secondary = LauncherColors.DarkSurfaceVariant,
            onSecondary = LauncherColors.DarkOnSurface,
            tertiary = accent.primaryDark,
            background = LauncherColors.DarkBackground,
            onBackground = LauncherColors.DarkOnBackground,
            surface = LauncherColors.DarkSurface,
            onSurface = LauncherColors.DarkOnSurface,
            surfaceVariant = LauncherColors.DarkSurfaceVariant,
            onSurfaceVariant = LauncherColors.DarkOnSurfaceVariant,
            outline = LauncherColors.DarkBorder,
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            onPrimary = LauncherColors.LightBackground,
            secondary = LauncherColors.LightSurfaceVariant,
            onSecondary = LauncherColors.LightOnSurface,
            tertiary = accent.primaryDark,
            background = LauncherColors.LightBackground,
            onBackground = LauncherColors.LightOnBackground,
            surface = LauncherColors.LightSurface,
            onSurface = LauncherColors.LightOnSurface,
            surfaceVariant = LauncherColors.LightSurfaceVariant,
            onSurfaceVariant = LauncherColors.LightOnSurfaceVariant,
            outline = LauncherColors.LightBorder,
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
