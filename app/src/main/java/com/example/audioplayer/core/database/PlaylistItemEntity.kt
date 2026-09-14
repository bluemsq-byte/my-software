package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.audioplayer.core.model.AudioSourceType

@Entity(
    tableName = "playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("playlistId")],
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val playlistId: Long,
    val position: Int,
    val mediaId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMillis: Long,
    val uri: String,
    val artworkUri: String?,
    val sourceType: AudioSourceType,
    val connectionId: String?,
    val remotePath: String?,
)