package dev.vive.kdelauncher.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

class IconPackManagerTest : FunSpec({

    context("component key formatting") {
        test("full component key is package/activity") {
            val pkg = "com.example.app"
            val activity = "com.example.app.MainActivity"
            val key = "$pkg/$activity"
            key shouldBe "com.example.app/com.example.app.MainActivity"
        }

        test("package-only key ends with slash") {
            val pkg = "com.example.app"
            val key = "$pkg/"
            key shouldBe "com.example.app/"
        }
    }

    context("ComponentInfo parsing") {
        test("strips ComponentInfo{} wrapper") {
            val raw = "ComponentInfo{com.example.app/com.example.app.MainActivity}"
            val clean = raw
                .removePrefix("ComponentInfo{")
                .removeSuffix("}")
            clean shouldBe "com.example.app/com.example.app.MainActivity"
        }

        test("handles already-clean component names") {
            val raw = "com.example.app/com.example.app.MainActivity"
            val clean = raw
                .removePrefix("ComponentInfo{")
                .removeSuffix("}")
            clean shouldBe "com.example.app/com.example.app.MainActivity"
        }

        test("handles partial ComponentInfo") {
            val raw = "ComponentInfo{com.example.app/.MainActivity}"
            val clean = raw
                .removePrefix("ComponentInfo{")
                .removeSuffix("}")
            clean shouldBe "com.example.app/.MainActivity"
        }
    }

    context("appfilter.xml entry parsing logic") {
        test("valid entry with component and drawable") {
            val component = "com.example.app/com.example.app.MainActivity"
            val drawable = "ic_example"

            val result = mutableMapOf<String, String>()
            if (component.isNotEmpty() && drawable.isNotEmpty()) {
                val clean = component
                    .removePrefix("ComponentInfo{")
                    .removeSuffix("}")
                result[clean] = drawable
            }

            result shouldHaveSize 1
            result["com.example.app/com.example.app.MainActivity"] shouldBe "ic_example"
        }

        test("skips entry with empty component") {
            val component = ""
            val drawable = "ic_example"

            val result = mutableMapOf<String, String>()
            if (component.isNotEmpty() && drawable.isNotEmpty()) {
                result[component] = drawable
            }

            result shouldHaveSize 0
        }

        test("skips entry with empty drawable") {
            val component = "com.example.app/.MainActivity"
            val drawable = ""

            val result = mutableMapOf<String, String>()
            if (component.isNotEmpty() && drawable.isNotEmpty()) {
                result[component] = drawable
            }

            result shouldHaveSize 0
        }

        test("parses multiple entries correctly") {
            val entries = listOf(
                "ComponentInfo{com.android.chrome/com.google.android.apps.chrome.Main}" to "chrome_icon",
                "ComponentInfo{org.mozilla.firefox/org.mozilla.gecko.BrowserApp}" to "firefox_icon",
                "" to "empty_component",
                "com.example/.Activity" to "",
            )

            val result = mutableMapOf<String, String>()
            entries.forEach { (component, drawable) ->
                if (component.isNotEmpty() && drawable.isNotEmpty()) {
                    val clean = component
                        .removePrefix("ComponentInfo{")
                        .removeSuffix("}")
                    result[clean] = drawable
                }
            }

            result shouldHaveSize 2
            result["com.android.chrome/com.google.android.apps.chrome.Main"] shouldBe "chrome_icon"
            result["org.mozilla.firefox/org.mozilla.gecko.BrowserApp"] shouldBe "firefox_icon"
        }
    }

    context("icon pack lookup strategy") {
        test("tries full component key first") {
            val filter = mapOf(
                "com.example.app/.MainActivity" to "icon_main",
                "com.example.app/" to "icon_default",
            )

            val componentKey = "com.example.app/.MainActivity"
            val pkgKey = "com.example.app/"

            val drawable = filter[componentKey] ?: filter[pkgKey]
            drawable shouldBe "icon_main"
        }

        test("falls back to package key when component not found") {
            val filter = mapOf(
                "com.example.app/" to "icon_default",
            )

            val componentKey = "com.example.app/.SettingsActivity"
            val pkgKey = "com.example.app/"

            val drawable = filter[componentKey] ?: filter[pkgKey]
            drawable shouldBe "icon_default"
        }

        test("returns null when neither key matches") {
            val filter = mapOf(
                "com.other.app/" to "icon_other",
            )

            val componentKey = "com.example.app/.MainActivity"
            val pkgKey = "com.example.app/"

            val drawable = filter[componentKey] ?: filter[pkgKey]
            drawable shouldBe null
        }
    }
})
