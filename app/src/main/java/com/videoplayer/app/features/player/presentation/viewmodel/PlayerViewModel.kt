package com.videoplayer.app.features.player.presentation.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val saveState: SavedStateHandle,
    val player: ExoPlayer
) : ViewModel() {

    init {
        setNewVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
    }

    private var videoUrl = saveState.get<String?>(CURRENT_VIDEO_URL)


    fun setNewVideoUrl(url: String) {
        saveState[CURRENT_VIDEO_URL] = url
        videoUrl = url

        if (player.isPlaying) {
            player.pause()
        }
        player.setMediaItem(MediaItem.fromUri(url.toUri()))
        player.prepare()
        player.playWhenReady = true
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}

const val CURRENT_VIDEO_URL = "CURRENT_VIDEO_URL"