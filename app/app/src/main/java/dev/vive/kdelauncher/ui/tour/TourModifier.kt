package dev.vive.kdelauncher.ui.tour

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

fun Modifier.tourTarget(
    target: TourTarget,
    tourState: TourState,
    onPositioned: (TourTarget, Rect) -> Unit
): Modifier = this.then(
    TourTargetElement(target, tourState, onPositioned)
)

private data class TourTargetElement(
    val target: TourTarget,
    val tourState: TourState,
    val onPositioned: (TourTarget, Rect) -> Unit
) : ModifierNodeElement<TourTargetNode>() {
    override fun create() = TourTargetNode(target, tourState, onPositioned)

    override fun update(node: TourTargetNode) {
        node.target = target
        node.tourState = tourState
        node.onPositioned = onPositioned
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "tourTarget"
        properties["target"] = target
        properties["tourState"] = tourState
    }
}

private class TourTargetNode(
    var target: TourTarget,
    var tourState: TourState,
    var onPositioned: (TourTarget, Rect) -> Unit
) : Modifier.Node(), GlobalPositionAwareModifierNode {
    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (tourState.isActive && tourState.currentStep()?.target == target) {
            val bounds = coordinates.boundsInWindow()
            onPositioned(target, bounds)
        }
    }
}
