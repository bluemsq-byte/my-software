package com.example.audioplayer.core.playback

import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.network.RemoteEntry

object RemotePlaybackQueueBuilder {
    fun build(
        entries: List<RemoteEntry>,
        sourceType: AudioSourceType,
        connectionId: String,
        uriFactory: (RemoteEntry) -> String,
    ): List<AudioTrack> {
        require(sourceType != AudioSourceType.LOCAL) {
            "Remote queue builder does not support local files"
        }

        return entries
            .asSequence()
            .filter { it.isAudioFile }
            .sortedBy { it.name.lowercase() }
            .map { entry ->
                AudioTrack(
                    id = "$connectionId:${entry.path}",
                    title = entry.name.substringBeforeLast('.'),
                    artist = null,
                    album = null,
                    durationMillis = 0L,
                    uri = uriFactory(entry),
                    sourceType = sourceType,
                    connectionId = connectionId,
                    remotePath = entry.path,
                )
            }
            .toList()
    }
}