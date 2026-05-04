package dev.vive.kdelauncher.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps icon name strings to actual Material ImageVector instances.
 * Used for persisting category icons as strings in SharedPreferences.
 */
object IconResolver {
    private val iconMap = mapOf(
        "Star" to Icons.Rounded.Star,
        "GridView" to Icons.Rounded.GridView,
        "Code" to Icons.Rounded.Code,
        "Palette" to Icons.Rounded.Palette,
        "Language" to Icons.Rounded.Language,
        "Gamepad" to Icons.Rounded.Gamepad,
        "MusicNote" to Icons.Rounded.MusicNote,
        "Settings" to Icons.Rounded.Settings,
        "FolderOpen" to Icons.Rounded.FolderOpen,
        "Favorite" to Icons.Rounded.Favorite,
        "Home" to Icons.Rounded.Home,
        "Work" to Icons.Rounded.Work,
        "School" to Icons.Rounded.School,
        "Rocket" to Icons.Rounded.RocketLaunch,
        "Terminal" to Icons.Rounded.Terminal,
        "Cloud" to Icons.Rounded.Cloud,
        "Camera" to Icons.Rounded.CameraAlt,
        "ShoppingCart" to Icons.Rounded.ShoppingCart,
        "Bolt" to Icons.Rounded.Bolt,
        "Diamond" to Icons.Rounded.Diamond,
        "Brush" to Icons.Rounded.Brush,
        "Build" to Icons.Rounded.Build,
        "Explore" to Icons.Rounded.Explore,
        "Forum" to Icons.Rounded.Forum,
        "Headphones" to Icons.Rounded.Headphones,
        "LocalCafe" to Icons.Rounded.LocalCafe,
        "Movie" to Icons.Rounded.Movie,
        "Newspaper" to Icons.Rounded.Newspaper,
        "Science" to Icons.Rounded.Science,
    )

    fun resolve(name: String): ImageVector {
        return iconMap[name] ?: Icons.Rounded.GridView
    }

    fun allEntries(): List<Pair<String, ImageVector>> {
        return iconMap.entries.map { it.key to it.value }
    }
}
