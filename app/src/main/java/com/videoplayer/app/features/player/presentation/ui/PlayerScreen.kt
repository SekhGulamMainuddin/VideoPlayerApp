package com.videoplayer.app.features.player.presentation.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.videoplayer.app.features.player.presentation.viewmodel.PlayerViewModel

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

    Scaffold() {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            AndroidView(
                { ctx ->
                    PlayerView(ctx).also { playerView ->
                        playerView.player = viewModel.player
                        playerView.setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },

                Modifier
                    .then(
                        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier.fillMaxSize()
                        else Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                16f/9f
                            )
                    )

            )
        }
    }
}