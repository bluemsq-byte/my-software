package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 当前播放队列。position 用于恢复顺序，媒体字段用于恢复完整歌曲信息。
 */
@Entity(
    tableName = "playback_queue",
    indices = [Index(value = ["position"], name = "index_playback_queue_position")],
)
data class PlaybackQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val position: Int,
    val mediaId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMillis: Long,
    val uri: String,
    val artworkUri: String?,
    val sourceType: String,
    val connectionId: String?,
    val remotePath: String?,
)
