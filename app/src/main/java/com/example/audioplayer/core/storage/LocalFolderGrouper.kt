package com.example.audioplayer.core.storage

import com.example.audioplayer.core.model.AudioTrack

data class LocalMusicFolder(
    val path: String,
    val name: String,
    val tracks: List<AudioTrack>,
)

object LocalFolderGrouper {
    fun group(tracks: List<AudioTrack>): List<LocalMusicFolder> {
        return tracks
            .groupBy { it.remotePath.orEmpty().ifBlank { "/" } }
            .map { (path, folderTracks) ->
                LocalMusicFolder(
                    path = path,
                    name = path.trimEnd('/').substringAfterLast('/').ifBlank { "内部存储" },
                    tracks = folderTracks.sortedBy { it.title.lowercase() },
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}