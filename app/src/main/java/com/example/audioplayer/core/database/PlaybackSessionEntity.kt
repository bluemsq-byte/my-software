package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 单例播放会话。id 固定为 1，只保存恢复播放所需的最小状态。
 */
@Entity(tableName = "playback_session")
data class PlaybackSessionEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val currentIndex: Int,
    val positionMillis: Long,
    val playbackMode: String,
    val updatedAtEpochMillis: Long,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
