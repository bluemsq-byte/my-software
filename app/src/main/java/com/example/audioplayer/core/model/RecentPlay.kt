package com.example.audioplayer.core.model

data class RecentPlay(
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
    val lastPlayedEpochMillis: Long,
    val playCount: Int,
)

fun RecentPlay.toAudioTrack() = AudioTrack(
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