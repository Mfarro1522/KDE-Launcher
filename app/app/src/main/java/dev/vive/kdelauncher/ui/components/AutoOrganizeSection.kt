package dev.vive.kdelauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.domain.usecase.SuggestAppOrganizationUseCase
import dev.vive.kdelauncher.ui.OrganizationSuggestionState
import dev.vive.kdelauncher.ui.theme.LauncherTypography
import dev.vive.kdelauncher.ui.theme.LocalColors
import dev.vive.kdelauncher.ui.theme.LocalLauncherAccent
import kotlinx.coroutines.delay

@Composable
fun AutoOrganizeSection(
    organizationSuggestionState: OrganizationSuggestionState,
    onSuggestOrganization: () -> Unit,
    onApplySuggestions: (List<SuggestAppOrganizationUseCase.Suggestion>) -> Unit,
    onCancelOrganization: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val accent = LocalLauncherAccent.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF8B5CF6)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Organización automática",
                    style = LauncherTypography.bodyMedium,
                    color = colors.onBackground
                )
                Text(
                    text = "Clasifica apps sin categoría usando reglas locales",
                    style = LauncherTypography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        when (organizationSuggestionState) {
            is OrganizationSuggestionState.Idle -> {
                Button(
                    onClick = onSuggestOrganization,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent.primaryBg,
                        contentColor = accent.primary
                    )
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Organizar apps automáticamente")
                }
            }

            is OrganizationSuggestionState.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = accent.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        "Analizando apps…",
                        style = LauncherTypography.bodyMedium,
                        color = colors.onBackground
                    )
                }
            }

            is OrganizationSuggestionState.Preview -> {
                val result = organizationSuggestionState.result
                val suggestions = result.suggestions

                Text(
                    text = "Sugerencias encontradas: ${suggestions.size}",
                    style = LauncherTypography.titleSmall,
                    color = colors.onBackground,
                    modifier = Modifier.padding(top = 10.dp)
                )

                if (suggestions.isEmpty()) {
                    Text(
                        text = "No hay apps sin categoría para reorganizar.",
                        style = LauncherTypography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.take(8).forEach { suggestion ->
                            SuggestionCard(
                                label = suggestion.label,
                                category = AppCategory.displayName(suggestion.proposedCategory),
                                categoryId = suggestion.proposedCategory
                            )
                        }

                        if (suggestions.size > 8) {
                            Text(
                                text = "… y ${suggestions.size - 8} más",
                                style = LauncherTypography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelOrganization,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onApplySuggestions(suggestions) },
                        modifier = Modifier.weight(1f),
                        enabled = suggestions.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = accent.primary)
                    ) {
                        Text("Aplicar")
                    }
                }
            }

            is OrganizationSuggestionState.Applied -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF10B981))
                    Spacer(Modifier.size(8.dp))
                    Text("Categorías aplicadas", color = Color(0xFF10B981))
                }
                LaunchedEffect(Unit) {
                    delay(2000)
                    onCancelOrganization()
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    label: String,
    category: String,
    categoryId: String
) {
    val colors = LocalColors.current
    val accent = LocalLauncherAccent.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.32f))
            .border(1.dp, colors.border.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = LauncherTypography.bodyMedium,
                color = colors.onBackground
            )
            Text(
                text = category,
                style = LauncherTypography.bodySmall,
                color = accent.primary
            )
        }
    }
}