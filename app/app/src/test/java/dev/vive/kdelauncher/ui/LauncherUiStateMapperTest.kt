package dev.vive.kdelauncher.ui

import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.data.model.ProfileType
import dev.vive.kdelauncher.ui.components.IconSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import dev.vive.kdelauncher.data.model.ColorTheme
import dev.vive.kdelauncher.data.model.AiProvider

class LauncherUiStateMapperTest : FunSpec({

    test("maps favorites and category overrides into ui state") {
        val personalApp = AppModel(
            packageName = "com.example.mail",
            activityName = ".Main",
            label = "Mail",
            category = AppCategory.INTERNET,
        )
        val workApp = AppModel(
            packageName = "com.example.docs",
            activityName = ".Main",
            label = "Docs",
            category = AppCategory.UTILITIES,
        )

        val input = baseInput(
            allApps = listOf(personalApp, workApp),
            favorites = setOf("com.example.mail"),
            workApps = setOf("com.example.docs"),
            categoryOverrides = mapOf(
                categoryOverrideKey(workApp, isWorkApp = true) to AppCategory.DEVELOPMENT
            )
        )
        val state = LauncherUiStateMapper.map(input, appContent(input))

        state.allApps.first { it.packageName == "com.example.mail" }.isFavorite shouldBe true
        state.allApps.first { it.packageName == "com.example.docs" }.profileTag shouldBe ProfileType.WORK
        state.allApps.first { it.packageName == "com.example.docs" }.category shouldBe AppCategory.DEVELOPMENT
    }

    test("filters by current profile and active category when there is no search query") {
        val input = baseInput(
            currentProfile = Profile.Work,
            activeCategory = AppCategory.DEVELOPMENT,
            allApps = listOf(
                AppModel("com.personal.notes", ".Main", "Notes", category = AppCategory.UTILITIES),
                AppModel("com.work.code", ".Main", "Code", category = AppCategory.DEVELOPMENT),
                AppModel("com.work.chat", ".Main", "Chat", category = AppCategory.INTERNET),
            ),
            workApps = setOf("com.work.code", "com.work.chat")
        )
        val state = LauncherUiStateMapper.map(input, appContent(input))

        state.filteredApps.map { it.packageName } shouldContainExactly listOf("com.work.code")
        state.appCounts shouldContain (AppCategory.ALL to 2)
        state.appCounts shouldContain (AppCategory.DEVELOPMENT to 1)
    }

    test("search filters across all apps regardless of active category") {
        val input = baseInput(
            searchQuery = "tele",
            activeCategory = AppCategory.GAMES,
            allApps = listOf(
                AppModel("com.chat.telegram", ".Main", "Telegram", category = AppCategory.INTERNET),
                AppModel("com.games.racing", ".Main", "Racing", category = AppCategory.GAMES),
            )
        )
        val state = LauncherUiStateMapper.map(input, appContent(input))

        state.filteredApps.map { it.packageName } shouldContainExactly listOf("com.chat.telegram")
    }

    test("real work profile detection uses userHandle instead of stored work app set") {
        val personalApp = AppModel(
            packageName = "com.example.personal",
            activityName = ".Main",
            label = "Personal"
        )
        val workApp = AppModel(
            packageName = "com.example.work",
            activityName = ".Main",
            label = "Work",
            userHandle = mockk(relaxed = true)
        )

        val input = baseInput(
            currentProfile = Profile.Work,
            hasRealWorkProfile = true,
            allApps = listOf(personalApp, workApp),
            workApps = emptySet()
        )
        val state = LauncherUiStateMapper.map(input, appContent(input))

        state.filteredApps.map { it.packageName } shouldContainExactly listOf("com.example.work")
        state.allApps.first { it.packageName == "com.example.work" }.profileTag shouldBe ProfileType.WORK
    }

    test("display-only settings reuse projected app content") {
        val input = baseInput(
            allApps = listOf(
                AppModel("com.chat.telegram", ".Main", "Telegram", category = AppCategory.INTERNET)
            )
        )
        val content = appContent(input)
        val updatedInput = input.copy(
            settingsDisplay = input.settingsDisplay.copy(iconSize = IconSize.LARGE),
            settingsIcon = input.settingsIcon.copy(
                gridColumns = 5,
                selectedIconPack = "pack.example"
            )
        )

        val state = LauncherUiStateMapper.map(updatedInput, content)

        (state.allApps === content.allApps) shouldBe true
        (state.filteredApps === content.filteredApps) shouldBe true
        state.gridColumns shouldBe 5
        state.iconSize shouldBe IconSize.LARGE
        state.selectedIconPack shouldBe "pack.example"
    }
})

private fun appContent(input: LauncherUiProjectionInput): LauncherAppContentState {
    return LauncherUiStateMapper.mapAppContent(
        LauncherAppContentInput(
            app = input.app,
            profile = input.profile,
            category = input.category,
            system = input.system
        )
    )
}

private fun baseInput(
    allApps: List<AppModel> = emptyList(),
    searchQuery: String = "",
    activeCategory: AppCategory = AppCategory.ALL,
    currentProfile: Profile = Profile.Personal,
    favorites: Set<String> = emptySet(),
    workApps: Set<String> = emptySet(),
    categoryOverrides: Map<String, AppCategory> = emptyMap(),
    hasRealWorkProfile: Boolean = false
): LauncherUiProjectionInput {
    return LauncherUiProjectionInput(
        app = LauncherAppInput(
            allApps = allApps,
            searchQuery = searchQuery,
            activeCategory = activeCategory
        ),
        settingsDisplay = LauncherSettingsDisplayInput(
            darkTheme = true,
            colorTheme = ColorTheme.SYSTEM,
            showAppLabels = true,
            showSettings = false,
            isLoading = false,
            iconSize = IconSize.MEDIUM
        ),
        settingsIcon = LauncherSettingsIconInput(
            showIconBackground = true,
            gridColumns = 3,
            selectedIconPack = null,
            isLoadingIconPacks = false,
            installedIconPacks = emptyList()
        ),
        profile = LauncherProfileInput(
            currentProfile = currentProfile,
            favorites = favorites,
            workApps = workApps
        ),
        category = LauncherCategoryInput(
            categoryConfigs = emptyList(),
            visibleCategories = AppCategory.entries,
            categoryOverrides = categoryOverrides
        ),
        system = LauncherSystemInput(
            isDefaultLauncher = true,
            hasRealWorkProfile = hasRealWorkProfile,
            isWorkProfileLocked = false
        ),
        labs = LauncherLabsInput(
            enabled = false,
            provider = AiProvider.GROQ,
            apiKey = null,
            model = null,
            modelsCache = emptyList(),
            isConnecting = false,
            connectionError = null,
            suggestions = emptyList(),
            isOrganizing = false,
            organizationError = null
        )
    )
}
