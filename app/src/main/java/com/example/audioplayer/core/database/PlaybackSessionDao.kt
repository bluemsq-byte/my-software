package com.example.audioplayer.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface PlaybackSessionDao {
    @Query("SELECT * FROM playback_session WHERE id = 1 LIMIT 1")
    suspend fun getSession(): PlaybackSessionEntity?

    @Query("SELECT * FROM playback_queue ORDER BY position")
    suspend fun getQueue(): List<PlaybackQueueEntity>

    @Upsert
    suspend fun upsertSession(session: PlaybackSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(items: List<PlaybackQueueEntity>)

    @Query("DELETE FROM playback_queue")
    suspend fun deleteQueue()

    @Query("DELETE FROM playback_session")
    suspend fun deleteSession()

    @Transaction
    suspend fun replace(
        session: PlaybackSessionEntity,
        queue: List<PlaybackQueueEntity>,
    ) {
        upsertSession(session)
        deleteQueue()
        if (queue.isNotEmpty()) insertQueue(queue)
    }

    @Transaction
    suspend fun clear() {
        deleteQueue()
        deleteSession()
    }
}
