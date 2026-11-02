package com.acme.fieldnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acme.fieldnotes.ui.FeedScreen
import com.acme.fieldnotes.ui.FeedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = FieldNotesColors) {
                FeedScreen(viewModel = viewModel { FeedViewModel() })
            }
        }
    }
}

private val FieldNotesColors = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF0B3D91),
    background = Color(0xFFF6F7F9),
    surface = Color(0xFFF6F7F9),
)
