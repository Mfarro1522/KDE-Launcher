package dev.vive.kdelauncher.data.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull

class ColorThemeTest : StringSpec({

    "fromName should return correct ColorTheme for valid names" {
        ColorTheme.fromName("DRACULA") shouldBe ColorTheme.DRACULA
        ColorTheme.fromName("TOKYO_NIGHT") shouldBe ColorTheme.TOKYO_NIGHT
    }

    "fromName should return SYSTEM for invalid or null names" {
        ColorTheme.fromName("INVALID_NAME") shouldBe ColorTheme.SYSTEM
        ColorTheme.fromName(null) shouldBe ColorTheme.SYSTEM
    }

    "fromName should be case insensitive" {
        ColorTheme.fromName("dracula") shouldBe ColorTheme.DRACULA
        ColorTheme.fromName("VERCEL") shouldBe ColorTheme.VERCEL
    }

    "all custom themes should have valid colors" {
        ColorTheme.entries.filter { it != ColorTheme.SYSTEM }.forEach { theme ->
            theme.backgroundArgb shouldNotBeNull { "Background for ${theme.name} is null" }
            theme.surfaceArgb shouldNotBeNull { "Surface for ${theme.name} is null" }
            theme.accentArgb shouldNotBeNull { "Accent for ${theme.name} is null" }
            theme.onBackgroundArgb shouldNotBeNull { "OnBackground for ${theme.name} is null" }
        }
    }
})
