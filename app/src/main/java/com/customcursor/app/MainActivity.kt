package com.customcursor.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.customcursor.app.ui.screens.DashboardScreen
import com.customcursor.app.ui.theme.CustomCursorTheme
import com.customcursor.app.viewmodel.CursorViewModel
class MainActivity : ComponentActivity() {
    private val viewModel: CursorViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CustomCursorTheme { DashboardScreen(viewModel) } }
    }
}
