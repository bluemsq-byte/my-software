package com.example.audioplayer.core.model

data class PlaylistSummary(
    val id: Long,
    val name: String,
    val itemCount: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

data class PlaylistItem(
    val id: Long = 0L,
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
) {
    fun toAudioTrack() = AudioTrack(
        id = mediaId,
        title = title,
        artist = artist,
        album = album,
        durationMillis = durationMillis,
        uri = uri,
        artworkUri = artworkUri,
        sourceType = sourceType,
        connectionId = connectionId,
        remotePath = remotePath,
    )
}