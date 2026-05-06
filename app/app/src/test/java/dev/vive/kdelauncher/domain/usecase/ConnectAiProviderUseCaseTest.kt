package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AiProvider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe

class ConnectAiProviderUseCaseTest : StringSpec({

    val useCase = ConnectAiProviderUseCase()

    "should fail when API key is blank" {
        val result = useCase(AiProvider.GROQ, "   ")
        result.shouldBeFailure()
        result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        result.exceptionOrNull()?.message?.shouldBe("La clave API no puede estar vacía")
    }

    "should fail with connection error when using invalid API key for Groq" {
        val result = useCase(AiProvider.GROQ, "invalid_key")
        result.shouldBeFailure()
        // It should try to connect and fail with an HTTP 401
    }

})
