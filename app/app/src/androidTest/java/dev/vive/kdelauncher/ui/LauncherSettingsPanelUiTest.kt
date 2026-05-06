package dev.vive.kdelauncher.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.vive.kdelauncher.data.IconPackInfo
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.ui.components.CategoryConfig
import dev.vive.kdelauncher.ui.components.IconSize
import dev.vive.kdelauncher.ui.components.LauncherSettingsPanel
import dev.vive.kdelauncher.ui.theme.KDELauncherTheme
import org.junit.Rule
import org.junit.Test

class LauncherSettingsPanelUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun launcherSettingsPanel_themeRowCallsToggle() {
        var toggled = false

        composeTestRule.setContent {
            KDELauncherTheme {
                LauncherSettingsPanel(
                    isDarkTheme = true,
                    showAppLabels = true,
                    iconSize = IconSize.MEDIUM,
                    showIconBackground = true,
                    gridColumns = 3,
                    categoryConfigs = settingsDefaultConfigs(),
                    installedIconPacks = emptyList(),
                    selectedIconPack = null,
                    isLoadingIconPacks = false,
                    onToggleTheme = { toggled = true },
                    onToggleAppLabels = {},
                    onIconSizeChange = {},
                    onIconBackgroundToggle = {},
                    onGridColumnsChange = {},
                    onCategoryRename = { _, _ -> },
                    onCategoryIconChange = { _, _ -> },
                    onCategoryToggleHidden = {},
                    onSelectIconPack = {},
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("theme-toggle-row").performClick()
        assert(toggled)
    }

    @Test
    fun launcherSettingsPanel_gridColumnsOptionCallsCallback() {
        var selectedColumns: Int? = null

        composeTestRule.setContent {
            KDELauncherTheme {
                LauncherSettingsPanel(
                    isDarkTheme = true,
                    showAppLabels = true,
                    iconSize = IconSize.MEDIUM,
                    showIconBackground = true,
                    gridColumns = 3,
                    categoryConfigs = settingsDefaultConfigs(),
                    installedIconPacks = listOf(IconPackInfo("pack.example", "Pack", null)),
                    selectedIconPack = null,
                    isLoadingIconPacks = false,
                    onToggleTheme = {},
                    onToggleAppLabels = {},
                    onIconSizeChange = {},
                    onIconBackgroundToggle = {},
                    onGridColumnsChange = { selectedColumns = it },
                    onCategoryRename = { _, _ -> },
                    onCategoryIconChange = { _, _ -> },
                    onCategoryToggleHidden = {},
                    onSelectIconPack = {},
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("grid-columns-4").performClick()
        assert(selectedColumns == 4)
    }

    @Test
    fun launcherSettingsPanel_resetConfirmationCallsReset() {
        var resetCalled = false

        composeTestRule.setContent {
            KDELauncherTheme {
                LauncherSettingsPanel(
                    isDarkTheme = true,
                    showAppLabels = true,
                    iconSize = IconSize.MEDIUM,
                    showIconBackground = true,
                    gridColumns = 3,
                    categoryConfigs = settingsDefaultConfigs(),
                    installedIconPacks = emptyList(),
                    selectedIconPack = null,
                    isLoadingIconPacks = false,
                    onToggleTheme = {},
                    onToggleAppLabels = {},
                    onIconSizeChange = {},
                    onIconBackgroundToggle = {},
                    onGridColumnsChange = {},
                    onCategoryRename = { _, _ -> },
                    onCategoryIconChange = { _, _ -> },
                    onCategoryToggleHidden = {},
                    onSelectIconPack = {},
                    onReset = { resetCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("reset-settings-row").performClick()
        composeTestRule.onNodeWithText("¿Restaurar todo?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restaurar").performClick()

        assert(resetCalled)
    }
}

private fun settingsDefaultConfigs(): List<CategoryConfig> {
    return AppCategory.entries.map { category ->
        CategoryConfig(
            category = category,
            displayName = category.displayName,
            iconName = "Star",
            isHidden = false
        )
    }
}
