package com.videoplayer.app.features.player.presentation.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    val player: ExoPlayer
) : ViewModel() {

    private var progressJob: Job? = null

    var videoState: VideoState? = savedState[CURRENT_VIDEO]

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) startProgressTracking()
                else stopProgressTracking()
            }
        })

        setNewVideoUrl(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            true
        )
    }

    fun setNewVideoUrl(url: String, restore: Boolean) {
        stopProgressTracking()

        val restorePosition =
            if (restore && videoState?.url == url) videoState!!.position else 0L

        videoState = VideoState(url, restorePosition)
        savedState[CURRENT_VIDEO] = videoState

        player.setMediaItem(MediaItem.fromUri(url.toUri()))
        player.prepare()
        player.seekTo(restorePosition)
        player.playWhenReady = true
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                yield()
                if (player.isPlaying) {
                    savedState[CURRENT_VIDEO] =
                        videoState?.copy(position = player.currentPosition)
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        stopProgressTracking()
        player.release()
        super.onCleared()
    }
}

data class VideoState(
    val url: String,
    val position: Long
) : Serializable

const val CURRENT_VIDEO = "CURRENT_VIDEO"
