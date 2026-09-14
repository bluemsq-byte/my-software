package com.example.audioplayer.core.repository

import com.example.audioplayer.core.database.PlaylistDao
import com.example.audioplayer.core.database.PlaylistEntity
import com.example.audioplayer.core.database.PlaylistItemEntity
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.PlaylistItem
import com.example.audioplayer.core.model.PlaylistSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
) {
    fun observeSummaries(): Flow<List<PlaylistSummary>> = playlistDao.observeSummaries().map {
        rows -> rows.map { row ->
            PlaylistSummary(
                id = row.id,
                name = row.name,
                itemCount = row.itemCount,
                createdAtEpochMillis = row.createdAtEpochMillis,
                updatedAtEpochMillis = row.updatedAtEpochMillis,
            )
        }
    }

    suspend fun getSummary(playlistId: Long): PlaylistSummary? {
        val row = playlistDao.getSummary(playlistId) ?: return null
        return PlaylistSummary(
            id = row.id,
            name = row.name,
            itemCount = row.itemCount,
            createdAtEpochMillis = row.createdAtEpochMillis,
            updatedAtEpochMillis = row.updatedAtEpochMillis,
        )
    }

    fun observeItems(playlistId: Long): Flow<List<PlaylistItem>> {
        return playlistDao.observeItems(playlistId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun create(name: String): Long {
        val now = System.currentTimeMillis()
        return playlistDao.upsertPlaylist(
            PlaylistEntity(
                name = name.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun rename(playlistId: Long, name: String) {
        val playlist = playlistDao.getPlaylist(playlistId) ?: return
        playlistDao.upsertPlaylist(
            playlist.copy(
                name = name.trim(),
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun delete(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addTracks(playlistId: Long, tracks: List<AudioTrack>) {
        if (tracks.isEmpty()) return
        val existing = playlistDao.getItems(playlistId)
        var position = (existing.maxOfOrNull(PlaylistItemEntity::position) ?: -1) + 1
        playlistDao.insertItems(
            tracks.map { track ->
                track.toPlaylistItem(playlistId, position++)
            },
        )
        touch(playlistId)
    }

    suspend fun removeItem(playlistId: Long, itemId: Long) {
        val remaining = playlistDao.getItems(playlistId)
            .filterNot { it.id == itemId }
            .mapIndexed { index, item -> item.copy(position = index) }
        playlistDao.replaceItems(playlistId, remaining)
        touch(playlistId)
    }

    suspend fun moveItem(playlistId: Long, itemId: Long, direction: Int) {
        val items = playlistDao.getItems(playlistId).toMutableList()
        val index = items.indexOfFirst { it.id == itemId }
        val target = index + direction
        if (index < 0 || target !in items.indices) return
        val moved = items.removeAt(index)
        items.add(target, moved)
        playlistDao.replaceItems(
            playlistId,
            items.mapIndexed { position, item -> item.copy(position = position) },
        )
        touch(playlistId)
    }

    private suspend fun touch(playlistId: Long) {
        val playlist = playlistDao.getPlaylist(playlistId) ?: return
        playlistDao.upsertPlaylist(
            playlist.copy(updatedAtEpochMillis = System.currentTimeMillis()),
        )
    }

    private fun AudioTrack.toPlaylistItem(playlistId: Long, position: Int) = PlaylistItemEntity(
        playlistId = playlistId,
        position = position,
        mediaId = id,
        title = title,
        artist = artist,
        album = album,
        durationMillis = durationMillis,
        uri = uri,
        artworkUri = artworkUri,
        sourceType = sourceType,
        connectionId = connectionId,
        remotePath = remotePath,
    )

    private fun PlaylistItemEntity.toModel() = PlaylistItem(
        id = id,
        playlistId = playlistId,
        position = position,
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
    )
}