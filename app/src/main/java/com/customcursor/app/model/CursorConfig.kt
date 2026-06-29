package com.customcursor.app.model
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
data class CursorConfig(
    val shape: CursorShape = CursorShape.ARROW,
    val sizeDp: Float = 32f,
    val fillColorArgb: Int = Color(0xFF00E5FF).toArgb(),
    val borderColorArgb: Int = Color(0xFF1A1A2E).toArgb(),
    val borderThicknessDp: Float = 2f,
    val opacity: Float = 1f,
    val isEnabled: Boolean = false
)
enum class CursorShape(val label: String) {
    ARROW("Arrow"), HAND("Hand"), CROSSHAIR("Crosshair"),
    CIRCLE("Circle"), DIAMOND("Diamond"), STAR("Star")
}
