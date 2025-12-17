package com.videoplayer.app.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.videoplayer.app.features.player.presentation.ui.PlayerScreen
import com.videoplayer.app.ui.theme.VideoPlayerAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoPlayerAppTheme {
                PlayerScreen()
            }
        }
    }
}
