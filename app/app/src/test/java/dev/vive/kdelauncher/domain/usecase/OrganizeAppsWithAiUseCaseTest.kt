package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.provider.AiProvider
import dev.vive.kdelauncher.data.repository.CategoryCache
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

class OrganizeAppsWithAiUseCaseTest : StringSpec({

    val categoryCache = mockk<CategoryCache>(relaxed = true)
    val promptBuilder = mockk<AiPromptBuilder>()
    val useCase = OrganizeAppsWithAiUseCase(categoryCache, promptBuilder)

    "excludes system, games and multimedia apps from AI candidates" {
        val apps = listOf(
            AppModel("com.android.settings", ".Main", "Settings", category = AppCategory.SYSTEM, isSystemApp = true),
            AppModel("com.game.app", ".Main", "Game", category = AppCategory.GAMES),
            AppModel("com.spotify.music", ".Main", "Spotify", category = AppCategory.MUSIC),
            AppModel("com.normal.app", ".Main", "Normal", category = AppCategory.SOCIAL)
        )

        val provider = mockk<AiProvider>()
        every { provider.name } returns "groq"
        every { provider.modelId } returns "model"
        every { promptBuilder.buildSystemPrompt() } returns "system"
        every { promptBuilder.buildUserPrompt(any(), any()) } returns "user"
        coEvery { provider.classify(any(), any()) } returns Result.success(
            """{"apps":[{"p":"com.normal.app","c":"social","i":"message-circle"}]}"""
        )

        val result = useCase(apps, provider, useCache = false)
        result.debug.candidateCount shouldBe 1
        result.debug.excludedCount shouldBe 3
        result.fromAi.size shouldBe 1
        result.fromAi.first().packageName shouldBe "com.normal.app"
    }

    "handles empty app list gracefully" {
        val provider = mockk<AiProvider>()
        every { provider.name } returns "groq"
        every { provider.modelId } returns "model"

        val result = useCase(emptyList(), provider, useCache = false)
        result.debug.candidateCount shouldBe 0
        result.debug.excludedCount shouldBe 0
        result.fromAi shouldBe emptyList()
    }
})