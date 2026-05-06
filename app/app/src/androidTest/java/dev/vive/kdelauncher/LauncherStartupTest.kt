package dev.vive.kdelauncher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LauncherStartupTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launcherStarts_andDisplaysFavoritesCategory() {
        // Smoke test: verify that the main activity starts up and renders the UI
        // We look for typical UI elements that should be visible on first launch
        composeTestRule.waitForIdle()
        
        // El tab o cabecera de "Favoritos" debería estar visible en el sidebar o grid
        // Si no hay favoritos, debería mostrarse el texto de estado vacío
        try {
            composeTestRule.onNodeWithText("Favoritos").assertIsDisplayed()
        } catch (e: AssertionError) {
            composeTestRule.onNodeWithText("Mantén presionado un app para agregarla a favoritos").assertIsDisplayed()
        }
    }
}
