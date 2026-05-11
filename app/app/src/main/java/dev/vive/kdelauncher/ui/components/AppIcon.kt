package dev.vive.kdelauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.ProfileType
import dev.vive.kdelauncher.ui.theme.LauncherColors
import dev.vive.kdelauncher.ui.theme.LauncherTypography
import dev.vive.kdelauncher.ui.theme.LocalColors

enum class IconSize {
    SMALL, MEDIUM, LARGE
}

fun getIconDimensions(size: IconSize): Pair<Float, Float> = when (size) {
    IconSize.SMALL -> 40f to 28f
    IconSize.MEDIUM -> 48f to 32f
    IconSize.LARGE -> 56f to 40f
}

fun parseIconSize(size: String): IconSize = when (size.lowercase()) {
    "small" -> IconSize.SMALL
    "large" -> IconSize.LARGE
    else -> IconSize.MEDIUM
}

@Composable
fun AppIcon(
    app: AppModel,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    showLabel: Boolean,
    iconSize: IconSize = IconSize.MEDIUM,
    showIconBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val isWork = app.profileTag == ProfileType.WORK
    val imageBitmap = remember(app.icon) { app.icon?.imageBitmap }
    val dimensions = remember(iconSize) { getIconDimensions(iconSize) }
    val containerSize = dimensions.first
    val iconSizeDp = dimensions.second

    // Use rememberUpdatedState so pointerInput (keyed by Unit) always calls
    // the latest callbacks without restarting the gesture detector.
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentOnClick() },
                    onLongPress = { currentOnLongPress() }
                )
            }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (showLabel) 6.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(containerSize.dp)
                .clip(RoundedCornerShape(14.dp))
                .then(
                    if (showIconBackground) {
                        Modifier
                            .background(
                                if (isWork) LauncherColors.AccentOrangeBgLight
                                else colors.surfaceVariant.copy(alpha = 0.8f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isWork) LauncherColors.AccentOrange.copy(alpha = 0.2f)
                                else colors.border.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    } else Modifier
                )
                .then(
                    if (isWork) {
                        Modifier.drawBehind {
                            drawCircle(
                                color = colors.background,
                                radius = 9.dp.toPx(),
                                center = Offset(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())
                            )
                            drawCircle(
                                color = LauncherColors.AccentOrange,
                                radius = 6.dp.toPx(),
                                center = Offset(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = app.label,
                    modifier = Modifier.size(iconSizeDp.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Android,
                    contentDescription = app.label,
                    modifier = Modifier.size((iconSizeDp * 0.6f).dp),
                    tint = if (isWork) LauncherColors.AccentOrange
                    else colors.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        if (showLabel) {
            Text(
                text = app.label,
                style = LauncherTypography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 72.dp)
            )
        }
    }
}
