package com.example.audioplayer.core.model

import com.example.audioplayer.core.network.RemoteEntry

enum class AudioSourceType {
    LOCAL,
    SMB,
    WEBDAV,
}

data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMillis: Long,
    val uri: String,
    val artworkUri: String? = null,
    val sourceType: AudioSourceType,
    val connectionId: String? = null,
    val remotePath: String? = null,
)