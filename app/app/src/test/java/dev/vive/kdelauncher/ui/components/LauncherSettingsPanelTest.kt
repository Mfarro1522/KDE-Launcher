package dev.vive.kdelauncher.ui.components

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LauncherSettingsPanelTest : FunSpec({

    test("calculateColumnRange adapts to narrow screens") {
        val range = calculateColumnRange(
            iconSize = IconSize.LARGE,
            showIconBackground = true,
            screenWidthDp = 320f
        )

        range shouldBe (2 to 4)
    }

    test("calculateColumnRange allows more columns on wide screens") {
        val range = calculateColumnRange(
            iconSize = IconSize.SMALL,
            showIconBackground = true,
            screenWidthDp = 720f
        )

        range shouldBe (4 to 6)
    }

    test("calculateColumnRange gives one extra column when icon background is hidden") {
        val withBackground = calculateColumnRange(
            iconSize = IconSize.MEDIUM,
            showIconBackground = true,
            screenWidthDp = 360f
        )
        val withoutBackground = calculateColumnRange(
            iconSize = IconSize.MEDIUM,
            showIconBackground = false,
            screenWidthDp = 360f
        )

        withoutBackground.first shouldBe withBackground.first
        (withoutBackground.second >= withBackground.second) shouldBe true
    }
})
