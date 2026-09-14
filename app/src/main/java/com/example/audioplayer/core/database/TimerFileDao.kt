package com.example.audioplayer.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TimerFileDao {
    @Query("SELECT * FROM timer_files WHERE timerId = :timerId ORDER BY position")
    suspend fun getForTimer(timerId: Long): List<TimerFileEntity>

    @Query("DELETE FROM timer_files WHERE timerId = :timerId")
    suspend fun deleteForTimer(timerId: Long)

    @Upsert
    suspend fun insertAll(files: List<TimerFileEntity>)
}