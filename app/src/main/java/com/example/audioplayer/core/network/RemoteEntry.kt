package com.example.audioplayer.core.network

data class RemoteEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModifiedEpochMillis: Long? = null,
) {
    val isAudioFile: Boolean
        get() = !isDirectory && AUDIO_EXTENSIONS.any { extension ->
            name.endsWith(".$extension", ignoreCase = true)
        }

    companion object {
        val AUDIO_EXTENSIONS = setOf(
            "mp3",
            "wav",
            "m4a",
            "aac",
            "flac",
            "ogg",
            "opus",
            "wma",
        )
    }
}