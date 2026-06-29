package com.customcursor.app.ui.theme
import android.app.Activity
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.*
import androidx.core.view.WindowCompat
private val DarkColorScheme = darkColorScheme(
    primary=md_theme_dark_primary, onPrimary=md_theme_dark_onPrimary,
    primaryContainer=md_theme_dark_primaryContainer, onPrimaryContainer=md_theme_dark_onPrimaryContainer,
    secondary=md_theme_dark_secondary, background=md_theme_dark_background,
    surface=md_theme_dark_surface, onBackground=md_theme_dark_onBackground,
    onSurface=md_theme_dark_onSurface, surfaceVariant=md_theme_dark_surfaceVariant,
    outline=md_theme_dark_outline
)
@Composable
fun CustomCursorTheme(content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        dynamicDarkColorScheme(LocalContext.current) else DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = md_theme_dark_background.toArgb()
        window.navigationBarColor = md_theme_dark_background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
    MaterialTheme(colorScheme=colorScheme, typography=Typography(), content=content)
}
