package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)