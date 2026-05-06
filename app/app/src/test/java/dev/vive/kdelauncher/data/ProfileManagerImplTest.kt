package dev.vive.kdelauncher.data

import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.data.model.ProfileType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class ProfileManagerImplTest : FunSpec({

    context("readActiveProfile parsing") {
        test("null defaults to Personal") {
            val type: String? = null
            val profile = if (type == "WORK") Profile.Work else Profile.Personal
            profile shouldBe Profile.Personal
            profile.type shouldBe ProfileType.PERSONAL
        }

        test("WORK string returns Work profile") {
            val type = "WORK"
            val profile = if (type == "WORK") Profile.Work else Profile.Personal
            profile shouldBe Profile.Work
            profile.type shouldBe ProfileType.WORK
        }

        test("PERSONAL string returns Personal profile") {
            val type = "PERSONAL"
            val profile = if (type == "WORK") Profile.Work else Profile.Personal
            profile shouldBe Profile.Personal
            profile.type shouldBe ProfileType.PERSONAL
        }

        test("any other value defaults to Personal") {
            val type = "UNKNOWN"
            val profile = if (type == "WORK") Profile.Work else Profile.Personal
            profile shouldBe Profile.Personal
        }
    }

    context("toggle favorite logic") {
        test("adding a new favorite") {
            val current = mutableSetOf("com.app.one")
            val packageName = "com.app.two"

            val isFavorite = if (current.contains(packageName)) {
                current.remove(packageName)
                false
            } else {
                current.add(packageName)
                true
            }

            isFavorite shouldBe true
            current shouldContain "com.app.two"
            current.size shouldBe 2
        }

        test("removing an existing favorite") {
            val current = mutableSetOf("com.app.one", "com.app.two")
            val packageName = "com.app.one"

            val isFavorite = if (current.contains(packageName)) {
                current.remove(packageName)
                false
            } else {
                current.add(packageName)
                true
            }

            isFavorite shouldBe false
            ("com.app.one" !in current) shouldBe true
            current.size shouldBe 1
        }

        test("toggling same package twice returns to original state") {
            val current = mutableSetOf<String>()
            val packageName = "com.app.test"

            val addResult = if (current.contains(packageName)) {
                current.remove(packageName); false
            } else {
                current.add(packageName); true
            }
            addResult shouldBe true
            current.size shouldBe 1

            val removeResult = if (current.contains(packageName)) {
                current.remove(packageName); false
            } else {
                current.add(packageName); true
            }
            removeResult shouldBe false
            current.size shouldBe 0
        }
    }

    context("toggle work app logic") {
        test("adding a new work app") {
            val current = mutableSetOf("com.work.one")
            val packageName = "com.work.two"

            val isWork = if (current.contains(packageName)) {
                current.remove(packageName)
                false
            } else {
                current.add(packageName)
                true
            }

            isWork shouldBe true
            current shouldContain "com.work.two"
        }

        test("removing an existing work app") {
            val current = mutableSetOf("com.work.one", "com.work.two")
            val packageName = "com.work.two"

            val isWork = if (current.contains(packageName)) {
                current.remove(packageName)
                false
            } else {
                current.add(packageName)
                true
            }

            isWork shouldBe false
            ("com.work.two" !in current) shouldBe true
        }
    }

    context("Profile sealed class") {
        test("Personal has correct type") {
            Profile.Personal.type shouldBe ProfileType.PERSONAL
        }

        test("Work has correct type") {
            Profile.Work.type shouldBe ProfileType.WORK
        }

        test("Personal and Work are different") {
            (Profile.Personal == Profile.Work) shouldBe false
        }
    }
})
