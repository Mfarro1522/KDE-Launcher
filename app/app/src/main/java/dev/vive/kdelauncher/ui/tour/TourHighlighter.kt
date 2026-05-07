package dev.vive.kdelauncher.ui.tour

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun TourHighlighter(
    targetBounds: Rect?,
    modifier: Modifier = Modifier
) {
    if (targetBounds == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(TourDefaults.PulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    with(LocalDensity.current) {
        val width = targetBounds.width.toDp()
        val height = targetBounds.height.toDp()
        val offsetX = targetBounds.left.toDp()
        val offsetY = targetBounds.top.toDp()

        Box(
            modifier = modifier
                .offset(x = offsetX, y = offsetY)
                .size(width = width, height = height)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}
