package com.customcursor.app.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.customcursor.app.model.CursorShape
import com.customcursor.app.service.CursorOverlayService
import com.customcursor.app.ui.components.ColorPickerDialog
import com.customcursor.app.ui.components.CursorPreviewCanvas
import com.customcursor.app.ui.theme.NeonCyan
import com.customcursor.app.viewmodel.CursorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: CursorViewModel) {
    val config by vm.config.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var showFill by remember { mutableStateOf(false) }
    var showBorder by remember { mutableStateOf(false) }
    var applied by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (applied) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Custom Cursor",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = NeonCyan
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = NeonCyan)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val serviceOn = isAccOn(ctx)
            val statusBrush = if (serviceOn)
                Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))
            else
                Brush.horizontalGradient(listOf(Color(0xFF4A148C), Color(0xFF6A1B9A)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusBrush)
                    .clickable(enabled = !serviceOn) { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (serviceOn) Icons.Filled.CheckCircle else Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (serviceOn) "Service: Active" else "Service: Off — Tap to enable",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            SectionCard("Live Preview") {
                CursorPreviewCanvas(config = config, modifier = Modifier.fillMaxWidth())
            }

            SectionCard("Shape") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CursorShape.entries) { shape ->
                        val isSelected = shape == config.shape
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clickable { vm.updateShape(shape) }
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Text(
                                text = shape.label,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            SectionCard("Size  ${config.sizeDp.toInt()} dp") {
                Slider(
                    value = config.sizeDp,
                    onValueChange = vm::updateSize,
                    valueRange = 16f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                    )
                )
            }

            SectionCard("Colors") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val colorItems = listOf(
                        "Fill" to Color(config.fillColorArgb),
                        "Border" to Color(config.borderColorArgb)
                    )
                    for ((index, item) in colorItems.withIndex()) {
                        val (label, color) = item
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clickable { if (index == 0) showFill = true else showBorder = true }
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    Modifier.size(36.dp)
                                        .background(color, RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                )
                                Column {
                                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                    Text("Tap to change", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                                }
                            }
                        }
                    }
                }
            }

            SectionCard("Border Thickness  ${"%.1f".format(config.borderThicknessDp)} dp") {
                Slider(
                    value = config.borderThicknessDp,
                    onValueChange = vm::updateBorderThickness,
                    valueRange = 0f..8f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                    )
                )
            }

            SectionCard("Opacity  ${(config.opacity * 100).toInt()}%") {
                Slider(
                    value = config.opacity,
                    onValueChange = vm::updateOpacity,
                    valueRange = 0.2f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = NeonCyan.copy(alpha = 0.2f)
                    )
                )
            }

            Button(
                onClick = {
                    vm.applyAndPersist()
                    val serviceIntent = Intent(ctx, CursorOverlayService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                        ctx.startForegroundService(serviceIntent)
                    else
                        ctx.startService(serviceIntent)
                    applied = true
                },
                modifier = Modifier.fillMaxWidth().height(60.dp).scale(scale),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                AnimatedVisibility(visible = applied, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
                    Row {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Text(
                    text = if (applied) "Applied!" else "Set Now",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showFill) {
        ColorPickerDialog(
            title = "Fill Color",
            currentColor = Color(config.fillColorArgb),
            onColorSelected = vm::updateFillColor,
            onDismiss = { showFill = false }
        )
    }
    if (showBorder) {
        ColorPickerDialog(
            title = "Border Color",
            currentColor = Color(config.borderColorArgb),
            onColorSelected = vm::updateBorderColor,
            onDismiss = { showBorder = false }
        )
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

fun isAccOn(ctx: Context): Boolean {
    val name = "${ctx.packageName}/.accessibility.CursorAccessibilityService"
    val enabled = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
    return enabled.split(':').any { it.equals(name, ignoreCase = true) }
}
