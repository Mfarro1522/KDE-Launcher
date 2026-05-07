package dev.vive.kdelauncher.ui.tour

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.tourTarget(
    target: TourTarget,
    tourState: TourState,
    onPositioned: (TourTarget, Rect) -> Unit
): Modifier = composed {
    if (tourState.isActive && tourState.currentStep()?.target == target) {
        onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            onPositioned(target, bounds)
        }
    } else {
        this
    }
}
