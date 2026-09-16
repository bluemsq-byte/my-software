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
import com.example.audioplayer.core.repository.PlaybackSessionRepository
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

enum class PlaybackMode {
    SEQUENTIAL,
    REPEAT_ALL,
    REPEAT_ONE,
}

data class QueueTrack(
    val mediaId: String,
    val title: String,
    val artist: String?,
    val isCurrent: Boolean,
)

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
    val playbackMode: PlaybackMode = PlaybackMode.SEQUENTIAL,
    val queue: List<QueueTrack> = emptyList(),
)

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recentPlayRepository: RecentPlayRepository,
    private val playbackSessionRepository: PlaybackSessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var progressJob: Job? = null
    private var tracksById = emptyMap<String, AudioTrack>()
    private var lastRecordedTrackId: String? = null
    private var lastPersistedAtMillis: Long = 0L
    private var restoringSession = false

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
                    scope.launch { restoreSessionIfNeeded(mediaController) }
                }
            },
            { runnable -> scope.launch(Dispatchers.Main) { runnable.run() } },
        )
    }

    fun currentTrack(): AudioTrack? = _state.value.currentTrackId?.let(tracksById::get)

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
        persistSession(force = true)
    }

    fun playPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun playNext(track: AudioTrack) {
        val mediaController = controller ?: return
        tracksById = tracksById + (track.id to track)
        val insertIndex = (mediaController.currentMediaItemIndex + 1)
            .coerceIn(0, mediaController.mediaItemCount)
        mediaController.addMediaItem(insertIndex, MediaItemFactory.create(track))
        persistSession(force = true)
    }

    fun addToQueue(track: AudioTrack) {
        val mediaController = controller ?: return
        tracksById = tracksById + (track.id to track)
        mediaController.addMediaItem(MediaItemFactory.create(track))
        persistSession(force = true)
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    fun setPlaybackMode(mode: PlaybackMode) {
        val mediaController = controller ?: return
        mediaController.repeatMode = when (mode) {
            PlaybackMode.SEQUENTIAL -> Player.REPEAT_MODE_OFF
            PlaybackMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
            PlaybackMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
        }
        persistSession(force = true)
    }

    fun seekToQueueItem(index: Int) {
        val mediaController = controller ?: return
        if (index in 0 until mediaController.mediaItemCount) {
            mediaController.seekTo(index, 0L)
            mediaController.play()
            persistSession(force = true)
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val mediaController = controller ?: return
        if (fromIndex in 0 until mediaController.mediaItemCount &&
            toIndex in 0 until mediaController.mediaItemCount
        ) {
            mediaController.moveMediaItem(fromIndex, toIndex)
            persistSession(force = true)
        }
    }

    fun removeQueueItem(index: Int) {
        val mediaController = controller ?: return
        if (index in 0 until mediaController.mediaItemCount) {
            mediaController.removeMediaItem(index)
            persistSession(force = true)
        }
    }

    fun seekTo(positionMillis: Long) {
        controller?.seekTo(positionMillis)
        persistSession(force = true)
    }

    fun stop() {
        controller?.pause()
        persistSession(force = true)
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
            persistSession(force = false)
        }
    }

    private fun Player.toUiState(): PlaybackUiState {
        val mediaItem = currentMediaItem
        val track = mediaItem?.mediaId?.let(tracksById::get)
        val queue = (0 until mediaItemCount).mapNotNull { index ->
            val item = getMediaItemAt(index)
            val queueTrack = tracksById[item.mediaId]
            if (queueTrack == null) {
                null
            } else {
                QueueTrack(
                    mediaId = queueTrack.id,
                    title = queueTrack.title,
                    artist = queueTrack.artist,
                    isCurrent = index == currentMediaItemIndex,
                )
            }
        }
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
            playbackMode = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> PlaybackMode.REPEAT_ONE
                Player.REPEAT_MODE_ALL -> PlaybackMode.REPEAT_ALL
                else -> PlaybackMode.SEQUENTIAL
            },
            queue = queue,
        )
    }

    private fun startProgressUpdates(controller: MediaController) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _state.value = controller.toUiState()
                persistSession(force = false)
                delay(500L)
            }
        }
    }

    private suspend fun restoreSessionIfNeeded(mediaController: MediaController) {
        if (mediaController.mediaItemCount > 0) return
        val session = playbackSessionRepository.load() ?: return
        if (session.queue.isEmpty()) return
        restoringSession = true
        try {
            tracksById = session.queue.associateBy(AudioTrack::id)
            mediaController.setMediaItems(
                session.queue.map(MediaItemFactory::create),
                session.currentIndex,
                session.positionMillis,
            )
            mediaController.repeatMode = when (session.playbackMode) {
                PlaybackMode.SEQUENTIAL -> Player.REPEAT_MODE_OFF
                PlaybackMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
                PlaybackMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
            }
            mediaController.prepare()
            mediaController.pause()
            _state.value = mediaController.toUiState().copy(isPlaying = false)
        } finally {
            restoringSession = false
        }
    }

    private fun persistSession(force: Boolean) {
        if (restoringSession) return
        val mediaController = controller ?: return
        val now = System.currentTimeMillis()
        if (!force && now - lastPersistedAtMillis < SESSION_PERSIST_INTERVAL_MILLIS) return
        lastPersistedAtMillis = now
        val queue = (0 until mediaController.mediaItemCount).mapNotNull { index ->
            val mediaId = mediaController.getMediaItemAt(index).mediaId
            tracksById[mediaId]
        }
        if (queue.isEmpty()) return
        val playbackMode = when (mediaController.repeatMode) {
            Player.REPEAT_MODE_ONE -> PlaybackMode.REPEAT_ONE
            Player.REPEAT_MODE_ALL -> PlaybackMode.REPEAT_ALL
            else -> PlaybackMode.SEQUENTIAL
        }
        scope.launch {
            playbackSessionRepository.save(
                queue = queue,
                currentIndex = mediaController.currentMediaItemIndex,
                positionMillis = mediaController.currentPosition.coerceAtLeast(0L),
                playbackMode = playbackMode,
            )
        }
    }

    private companion object {
        const val SESSION_PERSIST_INTERVAL_MILLIS = 3_000L
    }
}
