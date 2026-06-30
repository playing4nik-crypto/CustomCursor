package com.customcursor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ColorPickerDialog(
    title: String,
    currentColor: Color,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = listOf(
        Color(0xFF00E5FF), Color(0xFFBB86FC), Color(0xFFFF4081), Color(0xFFFFD600),
        Color(0xFF69F0AE), Color(0xFFFF6E40), Color(0xFFFFFFFF), Color(0xFF9E9E9E),
        Color(0xFF212121), Color(0xFF1A237E), Color(0xFF4A148C), Color(0xFF880E4F),
        Color(0xFFE65100), Color(0xFF1B5E20), Color(0xFF006064)
    )
    var selected by remember { mutableStateOf(currentColor) }
    var hexText by remember { mutableStateOf("#%08X".format(currentColor.toArgb())) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.fillMaxWidth().height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(selected)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(16.dp))
                for (row in palette.chunked(5)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in row) {
                            Box(
                                Modifier.size(40.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (c == selected) 3.dp else 1.dp,
                                        color = if (c == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selected = c
                                        hexText = "#%08X".format(c.toArgb())
                                    }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { v ->
                        hexText = v
                        runCatching {
                            selected = Color(java.lang.Long.parseLong(v.trimStart('#'), 16).toInt())
                        }
                    },
                    label = { Text("Hex ARGB") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onColorSelected(selected.toArgb()); onDismiss() }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
