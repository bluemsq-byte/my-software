package com.example.audioplayer.core.repository

import com.example.audioplayer.core.database.RecentPlayDao
import com.example.audioplayer.core.database.RecentPlayEntity
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.RecentPlay
import com.example.audioplayer.core.model.toAudioTrack
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RecentPlayRepository @Inject constructor(
    private val recentPlayDao: RecentPlayDao,
) {
    fun observeRecent(limit: Int = 20): Flow<List<RecentPlay>> {
        return recentPlayDao.observeRecent(limit).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun record(track: AudioTrack) {
        val existing = recentPlayDao.getById(track.id)
        recentPlayDao.upsert(
            RecentPlayEntity(
                mediaId = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMillis = track.durationMillis,
                uri = track.uri,
                artworkUri = track.artworkUri,
                sourceType = track.sourceType,
                connectionId = track.connectionId,
                remotePath = track.remotePath,
                lastPlayedEpochMillis = System.currentTimeMillis(),
                playCount = (existing?.playCount ?: 0) + 1,
            ),
        )
    }

    suspend fun clear() {
        recentPlayDao.clear()
    }

    fun asAudioTracks(items: List<RecentPlay>) = items.map(RecentPlay::toAudioTrack)

    private fun RecentPlayEntity.toModel() = RecentPlay(
        mediaId = mediaId,
        title = title,
        artist = artist,
        album = album,
        durationMillis = durationMillis,
        uri = uri,
        artworkUri = artworkUri,
        sourceType = sourceType,
        connectionId = connectionId,
        remotePath = remotePath,
        lastPlayedEpochMillis = lastPlayedEpochMillis,
        playCount = playCount,
    )
}