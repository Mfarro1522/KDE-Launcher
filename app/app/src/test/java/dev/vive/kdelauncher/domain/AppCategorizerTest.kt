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
        test("com.android.settings without system flag goes to tools/herramientas") {
            AppCategorizer.categorize("com.android.settings", -1) shouldBe AppCategory.HERRAMIENTAS
        }
    }

    context("development/tools apps") {
        test("com.termux -> herramientas") {
            AppCategorizer.categorize("com.termux", -1) shouldBe AppCategory.HERRAMIENTAS
        }
        test("com.github.android -> herramientas") {
            AppCategorizer.categorize("com.github.android", -1) shouldBe AppCategory.HERRAMIENTAS
        }
        test("com.google.android.apps.code -> herramientas") {
            AppCategorizer.categorize("com.google.android.apps.code", -1) shouldBe AppCategory.HERRAMIENTAS
        }
    }

    context("creativity/photos apps now map to tools or default to ALL") {
        test("com.google.android.apps.photos -> herramientas") {
            AppCategorizer.categorize("com.google.android.apps.photos", -1) shouldBe AppCategory.HERRAMIENTAS
        }
        test("package containing 'gallery' -> herramientas") {
            AppCategorizer.categorize("com.example.gallery.app", -1) shouldBe AppCategory.HERRAMIENTAS
        }
        test("com.niksoftware.snapseed -> herramientas") {
            AppCategorizer.categorize("com.niksoftware.snapseed", -1) shouldBe AppCategory.HERRAMIENTAS
        }
    }

    context("browser apps") {
        test("com.android.chrome -> herramientas") {
            AppCategorizer.categorize("com.android.chrome", -1) shouldBe AppCategory.HERRAMIENTAS
        }
    }

    context("social apps (now default to ALL)") {
        test("com.whatsapp -> ALL") {
            AppCategorizer.categorize("com.whatsapp", -1) shouldBe AppCategory.ALL
        }
        test("com.discord -> ALL") {
            AppCategorizer.categorize("com.discord", -1) shouldBe AppCategory.ALL
        }
    }

    context("game apps") {
        test("package containing 'game' token -> GAMES") {
            AppCategorizer.categorize("com.example.game.app", -1) shouldBe AppCategory.GAMES
        }
        test("package containing 'games' token -> GAMES") {
            AppCategorizer.categorize("com.google.android.play.games", -1) shouldBe AppCategory.GAMES
        }
    }

    context("multimedia apps") {
        test("com.spotify.music -> MULTIMEDIA") {
            AppCategorizer.categorize("com.spotify.music", -1) shouldBe AppCategory.MULTIMEDIA
        }
        test("package containing 'music' token -> MULTIMEDIA") {
            AppCategorizer.categorize("com.example.music.app", -1) shouldBe AppCategory.MULTIMEDIA
        }
        test("com.maxmpz.audioplayer -> MULTIMEDIA (audio token)") {
            AppCategorizer.categorize("com.maxmpz.audioplayer", -1) shouldBe AppCategory.MULTIMEDIA
        }
    }

    context("streaming/video apps") {
        test("com.google.android.youtube -> MULTIMEDIA") {
            AppCategorizer.categorize("com.google.android.youtube", -1) shouldBe AppCategory.MULTIMEDIA
        }
        test("com.netflix.mediaclient -> MULTIMEDIA") {
            AppCategorizer.categorize("com.netflix.mediaclient", -1) shouldBe AppCategory.MULTIMEDIA
        }
    }

    context("utility apps") {
        test("com.android.calculator2 -> HERRAMIENTAS") {
            AppCategorizer.categorize("com.android.calculator2", -1) shouldBe AppCategory.HERRAMIENTAS
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
            AppCategorizer.categorize("com.android.vending", -1) shouldBe AppCategory.COMPRAS
        }

        test("Facebook Pages Manager is resolved to ALL") {
            AppCategorizer.categorize("com.facebook.pages.app", -1) shouldBe AppCategory.ALL
        }
    }

    context("Android system category fallback") {
        test("CATEGORY_GAME falls back to GAMES") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_GAME) shouldBe AppCategory.GAMES
        }

        test("CATEGORY_AUDIO falls back to MULTIMEDIA") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_AUDIO) shouldBe AppCategory.MULTIMEDIA
        }

        test("CATEGORY_VIDEO falls back to MULTIMEDIA") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_VIDEO) shouldBe AppCategory.MULTIMEDIA
        }

        test("CATEGORY_IMAGE falls back to ALL") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_IMAGE) shouldBe AppCategory.ALL
        }

        test("CATEGORY_SOCIAL falls back to ALL") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_SOCIAL) shouldBe AppCategory.ALL
        }

        test("CATEGORY_MAPS falls back to HERRAMIENTAS") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_MAPS) shouldBe AppCategory.HERRAMIENTAS
        }

        test("CATEGORY_PRODUCTIVITY falls back to HERRAMIENTAS") {
            AppCategorizer.categorize("com.unknown.app", ApplicationInfo.CATEGORY_PRODUCTIVITY) shouldBe AppCategory.HERRAMIENTAS
        }

        test("unknown category falls back to ALL") {
            AppCategorizer.categorize("com.unknown.app", 999) shouldBe AppCategory.ALL
        }
    }
})