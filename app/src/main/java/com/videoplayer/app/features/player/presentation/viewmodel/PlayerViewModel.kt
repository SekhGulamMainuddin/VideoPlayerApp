package com.videoplayer.app.features.player.presentation.viewmodel

import android.media.MediaDrm
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    val player: ExoPlayer,
    private val trackSelector: DefaultTrackSelector
) : ViewModel() {

    private var progressJob: Job? = null

    var videoState: VideoState? = savedState[CURRENT_VIDEO]

    private val _videoQuality = MutableStateFlow<List<VideoQuality>>(emptyList())
    val videoQuality: StateFlow<List<VideoQuality>>
        get() = _videoQuality

    init {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) startProgressTracking()
                    else stopProgressTracking()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    if (playbackState == Player.STATE_READY) {
                        _videoQuality.value = getVideoQualities()
                    }
                }
            },
        )

        setNewVideoUrl(
            "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
            true
        )

        val mediaDrm = MediaDrm(C.WIDEVINE_UUID)
        Log.d("SEKH BRO ", mediaDrm.getPropertyString("securityLevel"))

    }

    fun setNewVideoUrl(url: String, restore: Boolean) {
        _videoQuality.value = emptyList()
        stopProgressTracking()

        val restorePosition =
            if (restore && videoState?.url == url) videoState!!.position else 0L

        videoState = VideoState(url, restorePosition)
        savedState[CURRENT_VIDEO] = videoState

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setDrmConfiguration(
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(
                        "https://proxy.uat.widevine.com/proxy?provider=widevine_test"
                    )
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
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

    fun getVideoQualities(): List<VideoQuality> {
        val qualities = mutableListOf<VideoQuality>()

        player.currentTracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_VIDEO) {
                repeat(group.length) { index ->
                    val format = group.getTrackFormat(index)

                    qualities.add(
                        VideoQuality(
                            group = group.mediaTrackGroup,
                            trackIndex = index,
                            width = format.width,
                            height = format.height,
                            bitrate = format.bitrate
                        )
                    )
                }
            }
        }

        return qualities
            .distinctBy { it.height }
            .sortedBy { it.height }
    }

    fun onQualitySelected(quality: VideoQuality?) {
        val builder = trackSelector.buildUponParameters()

        if (quality == null) {
            // AUTO
            builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
        } else {
            builder
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .addOverride(
                    TrackSelectionOverride(
                        quality.group,
                        listOf(quality.trackIndex)
                    )
                )
        }

        trackSelector.parameters = builder.build()
    }

    override fun onCleared() {
        stopProgressTracking()
        player.release()
        super.onCleared()
    }
}

data class VideoQuality(
    val group: TrackGroup,
    val trackIndex: Int,
    val width: Int,
    val height: Int,
    val bitrate: Int
)


data class VideoState(
    val url: String,
    val position: Long
) : Serializable

const val CURRENT_VIDEO = "CURRENT_VIDEO"
