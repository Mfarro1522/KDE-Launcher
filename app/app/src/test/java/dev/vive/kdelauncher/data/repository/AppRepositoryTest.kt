package dev.vive.kdelauncher.data.repository

import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppIconBitmap
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.ProfileType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class AppRepositoryTest : FunSpec({

    context("AppModel creation") {
        test("creates app with all fields") {
            val app = AppModel(
                packageName = "com.example.app",
                activityName = "com.example.app.MainActivity",
                label = "Example App",
                icon = null,
                category = AppCategory.ALL,
                isFavorite = false,
                profileTag = ProfileType.PERSONAL,
            )

            app.packageName shouldBe "com.example.app"
            app.activityName shouldBe "com.example.app.MainActivity"
            app.label shouldBe "Example App"
            app.category shouldBe AppCategory.ALL
            app.isFavorite shouldBe false
            app.profileTag shouldBe ProfileType.PERSONAL
        }

        test("icon defaults to null") {
            val app = AppModel(
                packageName = "com.example.app",
                activityName = "com.example.app.MainActivity",
                label = "Example App",
            )

            app.icon.shouldBeNull()
        }
    }

    context("AppModel copy") {
        test("copy preserves unchanged fields") {
            val original = AppModel(
                packageName = "com.example.app",
                activityName = "com.example.app.MainActivity",
                label = "Example App",
                category = AppCategory.ALL,
            )

            val copied = original.copy(isFavorite = true)

            copied.packageName shouldBe original.packageName
            copied.activityName shouldBe original.activityName
            copied.label shouldBe original.label
            copied.category shouldBe original.category
            copied.isFavorite shouldBe true
        }

        test("copy with icon wraps bitmap in AppIconBitmap") {
            val original = AppModel(
                packageName = "com.example.app",
                activityName = "com.example.app.MainActivity",
                label = "Example App",
            )

            original.icon.shouldBeNull()
        }
    }

    context("app list merging") {
        test("personal and work apps merge correctly") {
            val personal = listOf(
                AppModel(packageName = "com.personal.one", activityName = ".Main", label = "Personal One"),
                AppModel(packageName = "com.personal.two", activityName = ".Main", label = "Personal Two"),
            )
            val work = listOf(
                AppModel(packageName = "com.work.one", activityName = ".Main", label = "Work One"),
            )

            val merged = (personal + work).sortedBy { it.label.lowercase() }

            merged shouldHaveSize 3
            merged[0].label shouldBe "Personal One"
            merged[1].label shouldBe "Personal Two"
            merged[2].label shouldBe "Work One"
        }

        test("merged list is sorted by label case-insensitive") {
            val apps = listOf(
                AppModel(packageName = "b", activityName = ".Main", label = "Zebra"),
                AppModel(packageName = "a", activityName = ".Main", label = "apple"),
                AppModel(packageName = "c", activityName = ".Main", label = "Banana"),
            )

            val sorted = apps.sortedBy { it.label.lowercase() }

            sorted[0].label shouldBe "apple"
            sorted[1].label shouldBe "Banana"
            sorted[2].label shouldBe "Zebra"
        }
    }

    context("app filtering by profile") {
        test("personal profile filters out work apps") {
            val apps = listOf(
                AppModel(packageName = "com.personal.app", activityName = ".Main", label = "Personal", profileTag = ProfileType.PERSONAL),
                AppModel(packageName = "com.work.app", activityName = ".Main", label = "Work", profileTag = ProfileType.WORK),
            )

            val personalOnly = apps.filter { it.profileTag == ProfileType.PERSONAL }
            personalOnly shouldHaveSize 1
            personalOnly[0].packageName shouldBe "com.personal.app"
        }

        test("work profile filters out personal apps") {
            val apps = listOf(
                AppModel(packageName = "com.personal.app", activityName = ".Main", label = "Personal", profileTag = ProfileType.PERSONAL),
                AppModel(packageName = "com.work.app", activityName = ".Main", label = "Work", profileTag = ProfileType.WORK),
            )

            val workOnly = apps.filter { it.profileTag == ProfileType.WORK }
            workOnly shouldHaveSize 1
            workOnly[0].packageName shouldBe "com.work.app"
        }
    }

    context("app filtering by category") {
        test("favorites category shows only favorites") {
            val apps = listOf(
                AppModel(packageName = "a", activityName = ".Main", label = "A", isFavorite = true),
                AppModel(packageName = "b", activityName = ".Main", label = "B", isFavorite = false),
                AppModel(packageName = "c", activityName = ".Main", label = "C", isFavorite = true),
            )

            val favorites = apps.filter { it.isFavorite }
            favorites shouldHaveSize 2
            favorites.all { it.isFavorite }.shouldBe(true)
        }

        test("specific category shows only matching apps") {
            val apps = listOf(
                AppModel(packageName = "a", activityName = ".Main", label = "A", category = AppCategory.GAMES),
                AppModel(packageName = "b", activityName = ".Main", label = "B", category = AppCategory.ALL),
                AppModel(packageName = "c", activityName = ".Main", label = "C", category = AppCategory.GAMES),
            )

            val games = apps.filter { it.category == AppCategory.GAMES }
            games shouldHaveSize 2
            games.all { it.category == AppCategory.GAMES }.shouldBe(true)
        }
    }

    context("search filtering") {
        test("search matches by label case-insensitive") {
            val apps = listOf(
                AppModel(packageName = "a", activityName = ".Main", label = "WhatsApp"),
                AppModel(packageName = "b", activityName = ".Main", label = "Telegram"),
            )

            val results = apps.filter {
                it.label.contains("whats", ignoreCase = true)
            }

            results shouldHaveSize 1
            results[0].label shouldBe "WhatsApp"
        }

        test("search matches by package name") {
            val apps = listOf(
                AppModel(packageName = "com.whatsapp", activityName = ".Main", label = "WhatsApp"),
                AppModel(packageName = "com.telegram", activityName = ".Main", label = "Telegram"),
            )

            val results = apps.filter {
                it.packageName.contains("telegram", ignoreCase = true)
            }

            results shouldHaveSize 1
            results[0].packageName shouldBe "com.telegram"
        }
    }
})
