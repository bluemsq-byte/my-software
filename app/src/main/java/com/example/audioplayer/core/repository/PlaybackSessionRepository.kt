package com.example.audioplayer.core.repository

import com.example.audioplayer.core.database.PlaybackQueueEntity
import com.example.audioplayer.core.database.PlaybackSessionDao
import com.example.audioplayer.core.database.PlaybackSessionEntity
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.playback.PlaybackMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StoredPlaybackSession(
    val currentIndex: Int,
    val positionMillis: Long,
    val playbackMode: PlaybackMode,
    val queue: List<AudioTrack>,
)

@Singleton
class PlaybackSessionRepository @Inject constructor(
    private val dao: PlaybackSessionDao,
) {
    suspend fun load(): StoredPlaybackSession? = withContext(Dispatchers.IO) {
        val session = dao.getSession() ?: return@withContext null
        val queue = dao.getQueue().map { entity -> entity.toAudioTrack() }
        if (queue.isEmpty()) return@withContext null
        StoredPlaybackSession(
            currentIndex = session.currentIndex.coerceIn(queue.indices),
            positionMillis = session.positionMillis.coerceAtLeast(0L),
            playbackMode = runCatching {
                PlaybackMode.valueOf(session.playbackMode)
            }.getOrDefault(PlaybackMode.SEQUENTIAL),
            queue = queue,
        )
    }

    suspend fun save(
        queue: List<AudioTrack>,
        currentIndex: Int,
        positionMillis: Long,
        playbackMode: PlaybackMode,
    ) = withContext(Dispatchers.IO) {
        if (queue.isEmpty()) {
            dao.clear()
            return@withContext
        }
        val session = PlaybackSessionEntity(
            currentIndex = currentIndex.coerceIn(queue.indices),
            positionMillis = positionMillis.coerceAtLeast(0L),
            playbackMode = playbackMode.name,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
        dao.replace(
            session = session,
            queue = queue.mapIndexed { index, track -> track.toEntity(index) },
        )
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        dao.clear()
    }

    internal fun PlaybackQueueEntity.toAudioTrack() = AudioTrack(
        id = mediaId,
        title = title,
        artist = artist,
        album = album,
        durationMillis = durationMillis,
        uri = uri,
        artworkUri = artworkUri,
        sourceType = AudioSourceType.valueOf(sourceType),
        connectionId = connectionId,
        remotePath = remotePath,
    )

    internal fun AudioTrack.toEntity(position: Int) = PlaybackQueueEntity(
        position = position,
        mediaId = id,
        title = title,
        artist = artist,
        album = album,
        durationMillis = durationMillis,
        uri = uri,
        artworkUri = artworkUri,
        sourceType = sourceType.name,
        connectionId = connectionId,
        remotePath = remotePath,
    )
}
