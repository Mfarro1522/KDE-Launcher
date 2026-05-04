package dev.vive.kdelauncher.data.model

import androidx.compose.ui.graphics.Color

/**
 * Profile type for visual filtering of apps.
 */
enum class ProfileType {
    PERSONAL,
    WORK
}

/**
 * User profile configuration.
 */
data class Profile(
    val type: ProfileType,
    val name: String,
    val accentColor: Color,
    val accentColorSecondary: Color
) {
    companion object {
        val Personal = Profile(
            type = ProfileType.PERSONAL,
            name = "Personal",
            accentColor = Color(0xFF00BFA5), // Teal
            accentColorSecondary = Color(0xFF00897B)
        )
        val Work = Profile(
            type = ProfileType.WORK,
            name = "Trabajo",
            accentColor = Color(0xFFFF9100), // Orange
            accentColorSecondary = Color(0xFFF57C00)
        )
    }
}
