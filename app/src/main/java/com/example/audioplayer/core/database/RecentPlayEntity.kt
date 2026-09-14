package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.audioplayer.core.model.AudioSourceType

@Entity(tableName = "recent_plays")
data class RecentPlayEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMillis: Long,
    val uri: String,
    val artworkUri: String?,
    val sourceType: AudioSourceType,
    val connectionId: String?,
    val remotePath: String?,
    val lastPlayedEpochMillis: Long,
    val playCount: Int,
)