package dev.vive.kdelauncher.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.ui.components.AppGrid
import dev.vive.kdelauncher.ui.components.CategoryConfig
import dev.vive.kdelauncher.ui.theme.KDELauncherTheme
import org.junit.Rule
import org.junit.Test

class AppGridTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appGrid_showsFavoritesEmptyState() {
        composeTestRule.setContent {
            KDELauncherTheme {
                AppGrid(
                    apps = emptyList(),
                    searchQuery = "",
                    activeCategory = AppCategory.FAVORITES,
                    categoryConfigs = appGridDefaultConfigs(),
                    visibleCategories = AppCategory.entries,
                    showAppLabels = true,
                    onAppClick = {},
                    onToggleFavorite = {},
                    onAssignCategory = { _, _ -> },
                    onClearCategory = {},
                    onAppInfo = {},
                    onUninstall = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Mantén presionado un app para agregarla a favoritos")
            .assertIsDisplayed()
    }

    @Test
    fun appGrid_showsSearchResultsHeader() {
        composeTestRule.setContent {
            KDELauncherTheme {
                AppGrid(
                    apps = listOf(AppModel("com.chat.telegram", ".Main", "Telegram")),
                    searchQuery = "tele",
                    activeCategory = AppCategory.ALL,
                    categoryConfigs = appGridDefaultConfigs(),
                    visibleCategories = AppCategory.entries,
                    showAppLabels = true,
                    onAppClick = {},
                    onToggleFavorite = {},
                    onAssignCategory = { _, _ -> },
                    onClearCategory = {},
                    onAppInfo = {},
                    onUninstall = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Resultados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Telegram").assertIsDisplayed()
    }

    @Test
    fun appGrid_longPressOpensMenuAndInfoActionCallsCallback() {
        var infoOpenedFor: String? = null
        composeTestRule.setContent {
            KDELauncherTheme {
                AppGrid(
                    apps = listOf(AppModel("com.chat.telegram", ".Main", "Telegram")),
                    searchQuery = "",
                    activeCategory = AppCategory.ALL,
                    categoryConfigs = appGridDefaultConfigs(),
                    visibleCategories = AppCategory.entries,
                    showAppLabels = true,
                    onAppClick = {},
                    onToggleFavorite = {},
                    onAssignCategory = { _, _ -> },
                    onClearCategory = {},
                    onAppInfo = { infoOpenedFor = it.packageName },
                    onUninstall = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Telegram")
            .performTouchInput { longClick() }

        composeTestRule.onNodeWithText("Información").assertIsDisplayed()
        composeTestRule.onNodeWithText("Información").performClick()

        assert(infoOpenedFor == "com.chat.telegram")
    }
}

private fun appGridDefaultConfigs(): List<CategoryConfig> {
    return AppCategory.entries.map { category ->
        CategoryConfig(
            category = category,
            displayName = category.displayName,
            iconName = "Star",
            isHidden = false
        )
    }
}
