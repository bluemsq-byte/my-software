package com.example.audioplayer.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers ORDER BY hour, minute, id")
    fun observeAll(): Flow<List<TimerEntity>>

    @Query("SELECT * FROM timers WHERE enabled = 1 ORDER BY hour, minute, id")
    suspend fun getEnabled(): List<TimerEntity>

    @Query("SELECT * FROM timers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TimerEntity?

    @Upsert
    suspend fun upsert(timer: TimerEntity): Long

    @Delete
    suspend fun delete(timer: TimerEntity)
}