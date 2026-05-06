package dev.vive.kdelauncher.data

import dev.vive.kdelauncher.data.model.AppCategory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SettingsManagerImplTest : FunSpec({

    context("getDefaultIconName") {
        test("FAVORITES -> Star") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.FAVORITES) shouldBe "Star"
        }
        test("ALL -> GridView") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.ALL) shouldBe "GridView"
        }
        test("DEVELOPMENT -> Code") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.DEVELOPMENT) shouldBe "Code"
        }
        test("GRAPHICS -> Palette") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.GRAPHICS) shouldBe "Palette"
        }
        test("INTERNET -> Language") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.INTERNET) shouldBe "Language"
        }
        test("GAMES -> Gamepad") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.GAMES) shouldBe "Gamepad"
        }
        test("MULTIMEDIA -> MusicNote") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.MULTIMEDIA) shouldBe "MusicNote"
        }
        test("SYSTEM -> Settings") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.SYSTEM) shouldBe "Settings"
        }
        test("UTILITIES -> FolderOpen") {
            SettingsManagerImpl.getDefaultIconName(AppCategory.UTILITIES) shouldBe "FolderOpen"
        }
    }

    context("availableIcons") {
        test("contains all default icons") {
            val defaults = AppCategory.entries.map { SettingsManagerImpl.getDefaultIconName(it) }
            defaults.forEach { icon ->
                SettingsManagerImpl.availableIcons shouldContain icon
            }
        }

        test("has 29 icon options") {
            SettingsManagerImpl.availableIcons.size shouldBe 29
        }
    }

    context("category override encoding") {
        test("override key format is 'scope:packageName=CategoryName'") {
            val key = "personal:com.example.app"
            val category = AppCategory.GAMES
            val encoded = "$key=${category.name}"
            encoded shouldContain "="
            encoded shouldContain key
            encoded shouldContain category.name
        }

        test("parsing override entries") {
            val entries = setOf(
                "personal:com.example.app=GAMES",
                "work:com.work.app=INTERNET",
            )

            val parsed = entries.mapNotNull { entry ->
                val parts = entry.split("=", limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val key = parts[0]
                val category = runCatching { AppCategory.valueOf(parts[1]) }.getOrNull()
                if (category != null) key to category else null
            }.toMap()

            parsed shouldHaveSize 2
            parsed shouldContainKey "personal:com.example.app"
            parsed["personal:com.example.app"] shouldBe AppCategory.GAMES
            parsed["work:com.work.app"] shouldBe AppCategory.INTERNET
        }

        test("invalid override entries are skipped") {
            val entries = setOf(
                "valid:com.app=GAMES",
                "no-equals-here",
                "also-invalid:com.app=NONEXISTENT",
            )

            val parsed = entries.mapNotNull { entry ->
                val parts = entry.split("=", limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val key = parts[0]
                val category = runCatching { AppCategory.valueOf(parts[1]) }.getOrNull()
                if (category != null) key to category else null
            }.toMap()

            parsed shouldHaveSize 1
            parsed["valid:com.app"] shouldBe AppCategory.GAMES
        }
    }

    context("hidden categories set operations") {
        test("adding a category to hidden set") {
            val current = emptySet<String>()
            val category = AppCategory.GAMES
            val updated = current + category.name
            updated shouldContain "GAMES"
            updated.size shouldBe 1
        }

        test("removing a category from hidden set") {
            val current = setOf("GAMES", "SYSTEM")
            val category = AppCategory.GAMES
            val updated = current - category.name
            ("GAMES" !in updated) shouldBe true
            updated.size shouldBe 1
        }

        test("removing non-existent category does not change set") {
            val current = setOf("GAMES")
            val category = AppCategory.INTERNET
            val updated = current - category.name
            updated shouldBe current
        }
    }
})
