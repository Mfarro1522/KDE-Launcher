package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AiProvider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.types.shouldBeInstanceOf

class OrganizeAppsWithAiUseCaseTest : StringSpec({

    val useCase = OrganizeAppsWithAiUseCase()

    "should fail when API key is blank" {
        val result = useCase(emptyList(), emptyList(), AiProvider.GEMINI, "", "gemini-1.5-flash")
        result.shouldBeFailure()
        result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
    }

    "should handle network errors gracefully" {
        val result = useCase(emptyList(), emptyList(), AiProvider.COHERE, "invalid_key", "command-r")
        result.shouldBeFailure()
        // It will fail because the API key is invalid and connection will throw
    }
})
