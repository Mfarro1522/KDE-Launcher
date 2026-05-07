package dev.vive.kdelauncher.ui.tour

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun ProductTourOverlay(
    tourState: TourState,
    targetPositions: Map<TourTarget, Rect>,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Scrim de fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TourDefaults.ScrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true,
                    onClick = {}
                )
        )
        
        // 2. Highlighter del paso actual
        val currentStep = tourState.currentStep()
        val targetBounds = currentStep?.let { targetPositions[it.target] }
        
        AnimatedVisibility(
            visible = targetBounds != null,
            enter = fadeIn(tween(TourDefaults.AnimationDuration)),
            exit = fadeOut(tween(TourDefaults.AnimationDuration))
        ) {
            TourHighlighter(targetBounds = targetBounds)
        }
        
        // 3. Tooltip posicionado
        currentStep?.let { step ->
            val density = LocalDensity.current
            val screenWidth = LocalConfiguration.current.screenWidthDp
            val screenHeight = LocalConfiguration.current.screenHeightDp
            
            val tooltipPosition = remember(targetBounds, screenWidth, screenHeight) {
                calculateTooltipPosition(targetBounds, step, density, screenWidth, screenHeight)
            }
            
            val stepTitle = stringResource(id = step.titleRes)
            val stepDesc = stringResource(id = step.descriptionRes)

            val tooltipModifier = if (targetBounds == null) {
                Modifier.align(Alignment.Center)
            } else {
                Modifier.offset { tooltipPosition }
            }

            AnimatedVisibility(
                visible = tourState.isActive,
                enter = fadeIn(tween(TourDefaults.AnimationDuration)) + slideInVertically(
                    animationSpec = tween(TourDefaults.AnimationDuration),
                    initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(tween(TourDefaults.AnimationDuration)),
                modifier = tooltipModifier
            ) {
                TourTooltip(
                    step = step,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSkip = onSkip,
                    isFirst = tourState.currentStepIndex == 0,
                    isLast = tourState.currentStepIndex == tourState.steps.lastIndex,
                    modifier = Modifier.semantics {
                        contentDescription = "Tutorial de bienvenida. $stepTitle. $stepDesc"
                    }
                )
            }
        }
    }
}

private fun calculateTooltipPosition(
    targetBounds: Rect?,
    step: TourStep,
    density: Density,
    screenWidth: Int,
    screenHeight: Int
): IntOffset {
    if (targetBounds == null) return IntOffset.Zero

    with(density) {
        val screenWidthPx = screenWidth.dp.roundToPx()
        val screenHeightPx = screenHeight.dp.roundToPx()
        val marginPx = 16.dp.roundToPx()
        val tooltipWidthPx = 300.dp.roundToPx()
        
        return when (step.target) {
            is TourTarget.Banner, is TourTarget.ProfileHeader, is TourTarget.SearchBar -> {
                IntOffset(marginPx, targetBounds.bottom.toInt() + marginPx)
            }
            is TourTarget.SettingsButton -> {
                // Ensure it doesn't go out of left bound
                val x = (targetBounds.left.toInt() - tooltipWidthPx - marginPx).coerceAtLeast(marginPx)
                IntOffset(x, targetBounds.top.toInt())
            }
            is TourTarget.CategorySidebar -> {
                IntOffset(targetBounds.right.toInt() + marginPx, targetBounds.top.toInt())
            }
            is TourTarget.AppGrid -> {
                IntOffset(marginPx, targetBounds.top.toInt() + marginPx)
            }
            is TourTarget.Labs -> {
                // Ensure it doesn't go out of top bound
                val y = (targetBounds.top.toInt() - 200.dp.roundToPx()).coerceAtLeast(marginPx)
                IntOffset(marginPx, y)
            }
            else -> IntOffset.Zero
        }
    }
}
