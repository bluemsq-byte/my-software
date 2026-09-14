package com.example.audioplayer.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ConnectionEntity>>

    @Query("SELECT * FROM connections WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ConnectionEntity?

    @Upsert
    suspend fun upsert(connection: ConnectionEntity)

    @Delete
    suspend fun delete(connection: ConnectionEntity)
}