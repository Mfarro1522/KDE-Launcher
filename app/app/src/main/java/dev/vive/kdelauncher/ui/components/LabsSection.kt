package dev.vive.kdelauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vive.kdelauncher.data.model.AiProvider
import dev.vive.kdelauncher.data.model.CategorySuggestion
import dev.vive.kdelauncher.ui.AiConnectionState
import dev.vive.kdelauncher.ui.OrganizationState
import dev.vive.kdelauncher.ui.theme.LauncherTypography
import dev.vive.kdelauncher.ui.theme.LocalColors
import dev.vive.kdelauncher.ui.theme.LocalLauncherAccent

@Composable
fun LabsSection(
    labsEnabled: Boolean,
    onToggleLabs: (Boolean) -> Unit,
    aiProvider: AiProvider,
    aiConnectionState: AiConnectionState,
    aiModel: String,
    organizationState: OrganizationState,
    onConnectAi: (AiProvider, String) -> Unit,
    onDisconnectAi: () -> Unit,
    onSetAiModel: (String) -> Unit,
    onOrganizeApps: () -> Unit,
    onApplySuggestions: (List<CategorySuggestion>) -> Unit,
    onCancelOrganization: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val accent = LocalLauncherAccent.current
    
    var showConsentDialog by remember { mutableStateOf(false) }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text("Permiso de Privacidad") },
            text = { Text("Para usar TAPO Labs y organizar tus aplicaciones, necesitamos tu permiso para enviar la lista de tus apps instaladas a un proveedor de Inteligencia Artificial (Groq, Gemini o Cohere). Tus datos no se almacenan permanentemente.\n\n¿Aceptas activar esta función?") },
            confirmButton = {
                TextButton(onClick = {
                    onToggleLabs(true)
                    showConsentDialog = false
                }) { Text("Aceptar", color = accent.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false }) { Text("Cancelar", color = colors.onSurfaceVariant) }
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Toggle de Labs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.6f))
                .clickable { 
                    if (!labsEnabled) {
                        showConsentDialog = true
                    } else {
                        onToggleLabs(false)
                    }
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
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
                    Icon(
                        imageVector = Icons.Rounded.Science,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF8B5CF6)
                    )
                }
                Column {
                    Text(
                        text = "TAPO Labs",
                        style = LauncherTypography.bodyMedium,
                        color = colors.onBackground
                    )
                    Text(
                        text = "Funciones experimentales con IA",
                        style = LauncherTypography.bodySmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Switch(
                checked = labsEnabled,
                onCheckedChange = { 
                    if (it) {
                        showConsentDialog = true
                    } else {
                        onToggleLabs(false)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accent.primary,
                    checkedTrackColor = accent.primaryBg,
                    uncheckedThumbColor = colors.onSurfaceVariant,
                    uncheckedTrackColor = colors.surfaceVariant,
                )
            )
        }

        AnimatedVisibility(
            visible = labsEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Disclaimer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.1f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Experimental. Los LLMs pueden cometer errores. Tus datos de apps no se comparten permanentemente, solo se usan en el prompt.",
                        style = LauncherTypography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.onBackground.copy(alpha = 0.8f)
                    )
                }

                if (aiConnectionState is AiConnectionState.Connected) {
                    // Estado Conectado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Conectado a ${aiProvider.displayName}",
                                style = LauncherTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF10B981) // Verde
                            )
                            Text(
                                text = "Modelo: $aiModel",
                                style = LauncherTypography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = onDisconnectAi) {
                            Text("Desconectar", color = Color(0xFFEF4444)) // Red-500
                        }
                    }

                    if (organizationState is OrganizationState.Idle) {
                        Button(
                            onClick = onOrganizeApps,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent.primaryBg,
                                contentColor = accent.primary
                            )
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Organizar Apps Automáticamente")
                        }
                    } else if (organizationState is OrganizationState.Loading) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = accent.primary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Analizando apps...", style = LauncherTypography.bodyMedium, color = colors.onBackground)
                        }
                    } else if (organizationState is OrganizationState.Preview) {
                        Text(
                            "Sugerencias encontradas:",
                            style = LauncherTypography.titleSmall,
                            color = colors.onBackground
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            organizationState.suggestions.take(5).forEach { sug ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sug.packageName.substringAfterLast("."), style = LauncherTypography.bodySmall, color = colors.onBackground)
                                    Text("${sug.currentCategory?.name} → ${sug.suggestedCategory.name}", style = LauncherTypography.bodySmall, color = accent.primary)
                                }
                            }
                            if (organizationState.suggestions.size > 5) {
                                Text("... y ${organizationState.suggestions.size - 5} más", style = LauncherTypography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onCancelOrganization, modifier = Modifier.weight(1f)) {
                                Text("Cancelar")
                            }
                            Button(
                                onClick = { onApplySuggestions(organizationState.suggestions) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = accent.primary)
                            ) {
                                Text("Aplicar")
                            }
                        }
                    } else if (organizationState is OrganizationState.Applied) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF10B981).copy(alpha = 0.2f)).padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF10B981))
                            Spacer(Modifier.width(8.dp))
                            Text("Categorías aplicadas", color = Color(0xFF10B981))
                        }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            onCancelOrganization()
                        }
                    }
                } else {
                    // Estado No Conectado
                    var selectedProvider by remember { mutableStateOf(aiProvider) }
                    var apiKey by remember { mutableStateOf("") }
                    var isExpanded by remember { mutableStateOf(false) }

                    // Provider dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedProvider.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Proveedor IA") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { isExpanded = !isExpanded }) {
                                    Icon(Icons.Rounded.ArrowDropDown, "Expandir")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            AiProvider.entries.forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text(provider.displayName) },
                                    onClick = {
                                        selectedProvider = provider
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // API Key input
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    if (aiConnectionState is AiConnectionState.Error) {
                        Text(
                            text = "Error: ${aiConnectionState.message}",
                            style = LauncherTypography.bodySmall,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Button(
                        onClick = { onConnectAi(selectedProvider, apiKey) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = apiKey.isNotBlank() && aiConnectionState !is AiConnectionState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = accent.primary)
                    ) {
                        if (aiConnectionState is AiConnectionState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Conectar")
                        }
                    }
                }
            }
        }
    }
}
