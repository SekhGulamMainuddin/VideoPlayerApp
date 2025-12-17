package com.videoplayer.app.features.player.presentation.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.videoplayer.app.features.player.presentation.viewmodel.PlayerViewModel
import com.videoplayer.app.features.player.presentation.viewmodel.VideoQuality

@Composable
fun PlayerScreen() {
    val viewModel = hiltViewModel<PlayerViewModel>()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    DisposableEffect(configuration.orientation) {
        val window =
            (context as Activity).window ?: return@DisposableEffect onDispose {}
        val insetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            insetsController.apply {
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }

        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    val videoQuality by viewModel.videoQuality.collectAsStateWithLifecycle()
    var showQualityDialog by remember {
        mutableStateOf(false)
    }

    if(showQualityDialog) {
        QualityPickerDialog(
            videoQuality,
            onAuto = {
                showQualityDialog = false
                viewModel.onQualitySelected(null)
            },
            onQualitySelected = { q->
                showQualityDialog = false
                viewModel.onQualitySelected(q)
            },
            onDismiss = {
                showQualityDialog = false
            },
        )
    }

    Scaffold() {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            AndroidView(
                { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.player
                        setBackgroundColor(android.graphics.Color.BLACK)
                        useController = true
                        controllerAutoShow = true
                        controllerShowTimeoutMs = 3000

                        // THIS enables ⚙️ settings (Quality / Audio / Subtitles)
                        setShowSubtitleButton(true)
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)

                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },

                Modifier
                    .then(
                        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier.fillMaxSize()
                        else Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                16f / 9f
                            )
                    )
            )

            if(videoQuality.isNotEmpty())
            IconButton(
                {
                    showQualityDialog = true
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Menu, "", tint = Color.White)
            }
        }
    }
}

@Composable
fun QualityPickerDialog(
    qualities: List<VideoQuality>,
    onAuto: () -> Unit,
    onQualitySelected: (VideoQuality) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Video quality") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Auto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAuto() }
                        .padding(16.dp)
                )

                qualities.forEach { quality ->
                    Text(
                        "${quality.height}p",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQualitySelected(quality) }
                            .padding(16.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}
