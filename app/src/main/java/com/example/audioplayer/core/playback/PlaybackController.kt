@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.repository.RecentPlayRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val currentTrackId: String? = null,
    val title: String = "",
    val artist: String? = null,
    val artworkUri: String? = null,
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
)

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recentPlayRepository: RecentPlayRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var progressJob: Job? = null
    private var tracksById = emptyMap<String, AudioTrack>()
    private var lastRecordedTrackId: String? = null

    fun connect() {
        if (controller != null) return
        val token = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java),
        )
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            {
                controller = future.get().also { mediaController ->
                    mediaController.addListener(playerListener)
                    _state.value = mediaController.toUiState()
                    startProgressUpdates(mediaController)
                }
            },
            { runnable -> scope.launch(Dispatchers.Main) { runnable.run() } },
        )
    }

    fun play(tracks: List<AudioTrack>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        tracksById = tracks.associateBy(AudioTrack::id)
        val controller = controller ?: return
        controller.setMediaItems(
            tracks.map(MediaItemFactory::create),
            startIndex.coerceIn(tracks.indices),
            0L,
        )
        controller.prepare()
        controller.play()
    }

    fun playPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMillis: Long) {
        controller?.seekTo(positionMillis)
    }

    fun stop() {
        controller?.pause()
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            val mediaId = player.currentMediaItem?.mediaId
            if (mediaId != null && mediaId != lastRecordedTrackId) {
                lastRecordedTrackId = mediaId
                tracksById[mediaId]?.let { track ->
                    scope.launch { recentPlayRepository.record(track) }
                }
            }
            _state.value = player.toUiState()
        }
    }

    private fun Player.toUiState(): PlaybackUiState {
        val mediaItem = currentMediaItem
        val track = mediaItem?.mediaId?.let(tracksById::get)
        return PlaybackUiState(
            isConnected = true,
            isPlaying = isPlaying,
            currentTrackId = mediaItem?.mediaId,
            title = track?.title ?: mediaItem?.mediaMetadata?.title?.toString().orEmpty(),
            artist = track?.artist ?: mediaItem?.mediaMetadata?.artist?.toString(),
            artworkUri = track?.artworkUri ?: mediaItem?.mediaMetadata?.artworkUri?.toString(),
            positionMillis = currentPosition.coerceAtLeast(0L),
            durationMillis = duration.coerceAtLeast(0L),
            hasPrevious = hasPreviousMediaItem(),
            hasNext = hasNextMediaItem(),
        )
    }

    private fun startProgressUpdates(controller: MediaController) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _state.value = controller.toUiState()
                delay(500L)
            }
        }
    }
}