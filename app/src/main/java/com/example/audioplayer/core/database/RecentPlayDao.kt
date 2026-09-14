package com.example.audioplayer.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayDao {
    @Query("SELECT * FROM recent_plays ORDER BY lastPlayedEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentPlayEntity>>

    @Query("SELECT * FROM recent_plays WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getById(mediaId: String): RecentPlayEntity?

    @Upsert
    suspend fun upsert(item: RecentPlayEntity)

    @Query("DELETE FROM recent_plays")
    suspend fun clear()
}