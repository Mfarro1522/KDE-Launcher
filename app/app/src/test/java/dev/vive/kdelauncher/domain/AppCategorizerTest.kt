package dev.vive.kdelauncher.domain

import android.content.pm.ApplicationInfo
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppCategorizer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppCategorizerTest : FunSpec({

    context("system apps") {
        test("isSystemApp=true returns SYSTEM regardless of package name") {
            AppCategorizer.categorize("com.example.game", -1, isSystemApp = true) shouldBe AppCategory.SYSTEM
        }
        test("com.android.settings without system flag goes to utilities") {
            AppCategorizer.categorize("com.android.settings", -1) shouldBe AppCategory.UTILITIES
        }
    }

    context("development apps") {
        test("com.termux -> development") {
            AppCategorizer.categorize("com.termux", -1) shouldBe "development"
        }
        test("com.github.android -> development") {
            AppCategorizer.categorize("com.github.android", -1) shouldBe "development"
        }
        test("com.google.android.apps.code -> development") {
            AppCategorizer.categorize("com.google.android.apps.code", -1) shouldBe "development"
        }
    }

    context("creativity apps") {
        test("com.google.android.apps.photos -> creativity") {
            AppCategorizer.categorize("com.google.android.apps.photos", -1) shouldBe "creativity"
        }
        test("package containing 'gallery' -> creativity") {
            AppCategorizer.categorize("com.example.gallery.app", -1) shouldBe "creativity"
        }
        test("com.niksoftware.snapseed -> creativity") {
            AppCategorizer.categorize("com.niksoftware.snapseed", -1) shouldBe "creativity"
        }
    }

    context("browser apps") {
        test("com.android.chrome -> browsers") {
            AppCategorizer.categorize("com.android.chrome", -1) shouldBe "browsers"
        }
    }

    context("social apps") {
        test("com.whatsapp -> social") {
            AppCategorizer.categorize("com.whatsapp", -1) shouldBe AppCategory.SOCIAL
        }
        test("com.discord -> social") {
            AppCategorizer.categorize("com.discord", -1) shouldBe AppCategory.SOCIAL
        }
    }

    context("game apps") {
        test("package containing 'game' -> GAMES") {
            AppCategorizer.categorize("com.example.game.app", -1) shouldBe AppCategory.GAMES
        }
        test("package containing 'play' -> GAMES") {
            AppCategorizer.categorize("com.google.android.play.games", -1) shouldBe AppCategory.GAMES
        }
    }

    context("music apps") {
        test("com.spotify.music -> MUSIC") {
            AppCategorizer.categorize("com.spotify.music", -1) shouldBe AppCategory.MUSIC
        }
        test("package containing 'music' -> MUSIC") {
            AppCategorizer.categorize("com.example.music.app", -1) shouldBe AppCategory.MUSIC
        }
        test("com.maxmpz.audioplayer -> MUSIC (audio token)") {
            AppCategorizer.categorize("com.maxmpz.audioplayer", -1) shouldBe AppCategory.MUSIC
        }
    }

    context("streaming apps") {
        test("com.google.android.youtube -> STREAMING") {
            AppCategorizer.categorize("com.google.android.youtube", -1) shouldBe AppCategory.STREAMING
        }
        test("com.netflix.mediaclient -> STREAMING") {
            AppCategorizer.categorize("com.netflix.mediaclient", -1) shouldBe AppCategory.STREAMING
        }
    }

    context("utility apps") {
        test("com.android.calculator2 -> UTILITIES") {
            AppCategorizer.categorize("com.android.calculator2", -1) shouldBe AppCategory.UTILITIES
        }
        test("com.android.contacts -> social") {
            AppCategorizer.categorize("com.android.contacts", -1) shouldBe AppCategory.SOCIAL
        }
    }

    context("unknown apps default to ALL") {
        test("com.example.unknown -> ALL") {
            AppCategorizer.categorize("com.example.unknown", -1) shouldBe AppCategory.ALL
        }
        test("org.random.app -> ALL") {
            AppCategorizer.categorize("org.random.app", -1) shouldBe AppCategory.ALL
        }
    }

    context("regression: known conflict cases") {
        test("Google Play Store (com.android.vending) is resolved by exact allowlist") {
            AppCategorizer.categorize("com.android.vending", -1) shouldBe "shopping"
        }

        test("Facebook Pages Manager is resolved by exact allowlist") {
            AppCategorizer.categorize("com.facebook.pages.app", -1) shouldBe AppCategory.SOCIAL
        }
    }

    context("Android system category fallback") {
        test("CATEGORY_GAME falls back to GAMES") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_GAME) shouldBe AppCategory.GAMES
        }

        test("CATEGORY_AUDIO falls back to MUSIC") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_AUDIO) shouldBe AppCategory.MUSIC
        }

        test("CATEGORY_VIDEO falls back to STREAMING") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_VIDEO) shouldBe AppCategory.STREAMING
        }

        test("CATEGORY_IMAGE falls back to creativity") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_IMAGE) shouldBe "creativity"
        }

        test("CATEGORY_SOCIAL falls back to SOCIAL") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_SOCIAL) shouldBe AppCategory.SOCIAL
        }

        test("CATEGORY_MAPS falls back to travel") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_MAPS) shouldBe "travel"
        }

        test("CATEGORY_PRODUCTIVITY falls back to PRODUCTIVITY") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_PRODUCTIVITY) shouldBe AppCategory.PRODUCTIVITY
        }

        test("unknown category falls back to ALL") {
            AppCategorizer.categorize("com.unknown.app", 999) shouldBe AppCategory.ALL
        }
    }
})