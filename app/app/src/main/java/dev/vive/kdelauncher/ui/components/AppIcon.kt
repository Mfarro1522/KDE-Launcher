package dev.vive.kdelauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.ProfileType
import dev.vive.kdelauncher.ui.theme.LauncherColors
import dev.vive.kdelauncher.ui.theme.LauncherTypography
import dev.vive.kdelauncher.ui.theme.LocalColors
import dev.vive.kdelauncher.ui.theme.LocalLauncherAccent

/**
 * Icon size options
 */
enum class IconSize {
    SMALL, MEDIUM, LARGE
}

/**
 * Returns dimensions for each icon size
 * Pair of (containerSize, iconSize)
 */
fun getIconDimensions(size: IconSize): Pair<Float, Float> = when (size) {
    IconSize.SMALL -> 40f to 28f   // 40dp container, 28dp icon
    IconSize.MEDIUM -> 48f to 32f  // 48dp container, 32dp icon (original)
    IconSize.LARGE -> 56f to 40f   // 56dp container, 40dp icon
}

/**
 * Parses icon size from string
 */
fun parseIconSize(size: String): IconSize = when (size.lowercase()) {
    "small" -> IconSize.SMALL
    "large" -> IconSize.LARGE
    else -> IconSize.MEDIUM
}

@Composable
fun AppIcon(
    app: AppModel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAssignCategory: (AppCategory) -> Unit,
    onClearCategory: () -> Unit,
    onRequestIcon: (AppModel) -> Unit,
    activeCategory: AppCategory,
    categoryConfigs: List<CategoryConfig>,
    visibleCategories: List<AppCategory>,
    showLabel: Boolean,
    iconSize: IconSize = IconSize.MEDIUM,
    showIconBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val accent = LocalLauncherAccent.current
    var isPressed by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100), label = "appScale"
    )

    val imageBitmap = remember(app.iconBitmap) {
        app.iconBitmap?.asImageBitmap()
    }

    LaunchedEffect(app.packageName, app.activityName, app.userHandle, app.iconBitmap) {
        if (app.iconBitmap == null) onRequestIcon(app)
    }

    val categoryOptions = remember(visibleCategories, categoryConfigs) {
        visibleCategories
            .filter { it != AppCategory.FAVORITES && it != AppCategory.ALL }
            .map { cat ->
                val name = categoryConfigs.find { it.category == cat }?.displayName
                    ?: cat.displayName
                cat to name
            }
    }

    val removeAction = if (activeCategory == AppCategory.FAVORITES)
        onToggleFavorite else onClearCategory
    val menuShape = RoundedCornerShape(14.dp)
    val menuItemBg = colors.surfaceVariant.copy(alpha = 0.7f)
    val favoriteIcon = if (app.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder
    val showRemoveAction = activeCategory != AppCategory.ALL

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try { awaitRelease() } finally { isPressed = false }
                        },
                        onTap = { onClick() },
                        onLongPress = { menuExpanded = true }
                    )
                }
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (showLabel) 6.dp else 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                val isWork = app.profileTag == ProfileType.WORK
                val (containerSize, iconSizeDp) = getIconDimensions(iconSize)
                
                Box(
                    modifier = Modifier
                        .size(containerSize.dp)
                        .then(
                            if (showIconBackground) {
                                Modifier
                                    .clip(RoundedCornerShape(14.dp))
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
                            } else {
                                Modifier
                            }
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

                if (app.profileTag == ProfileType.WORK) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colors.background)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(LauncherColors.AccentOrange)
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

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            offset = DpOffset(6.dp, 6.dp),
            modifier = Modifier
                .widthIn(min = 140.dp)
                .clip(menuShape)
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.8f), menuShape)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(menuItemBg)
                        .clickable {
                            menuExpanded = false
                            onToggleFavorite()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = favoriteIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (app.isFavorite) accent.primary else colors.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(menuItemBg)
                        .clickable {
                            menuExpanded = false
                            showCategoryPicker = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant
                    )
                }

                if (showRemoveAction) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(menuItemBg)
                            .clickable {
                                menuExpanded = false
                                removeAction()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Elegir categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categoryOptions.forEach { (category, label) ->
                        TextButton(onClick = {
                            onAssignCategory(category)
                            showCategoryPicker = false
                        }) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
