package com.example.audioplayer.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query(
        """
        SELECT p.id, p.name, COUNT(i.id) AS itemCount,
               p.createdAtEpochMillis, p.updatedAtEpochMillis
        FROM playlists p
        LEFT JOIN playlist_items i ON i.playlistId = p.id
        GROUP BY p.id
        ORDER BY p.updatedAtEpochMillis DESC
        """,
    )
    fun observeSummaries(): Flow<List<PlaylistSummaryRow>>

    @Query(
        """
        SELECT p.id, p.name, COUNT(i.id) AS itemCount,
               p.createdAtEpochMillis, p.updatedAtEpochMillis
        FROM playlists p
        LEFT JOIN playlist_items i ON i.playlistId = p.id
        WHERE p.id = :playlistId
        GROUP BY p.id
        """,
    )
    suspend fun getSummary(playlistId: Long): PlaylistSummaryRow?

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylist(playlistId: Long): PlaylistEntity?

    @Upsert
    suspend fun upsertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position")
    fun observeItems(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position")
    suspend fun getItems(playlistId: Long): List<PlaylistItemEntity>

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun deleteItems(playlistId: Long)

    @Upsert
    suspend fun insertItems(items: List<PlaylistItemEntity>)

    @Transaction
    suspend fun replaceItems(playlistId: Long, items: List<PlaylistItemEntity>) {
        deleteItems(playlistId)
        if (items.isNotEmpty()) insertItems(items)
    }
}