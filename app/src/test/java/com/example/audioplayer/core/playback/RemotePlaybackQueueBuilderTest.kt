package com.example.audioplayer.core.playback

import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.network.RemoteEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RemotePlaybackQueueBuilderTest {
    @Test
    fun build_filtersNonAudioAndSortsByDisplayName() {
        val tracks = RemotePlaybackQueueBuilder.build(
            entries = listOf(
                RemoteEntry("收藏.txt", "/收藏.txt", false),
                RemoteEntry("B.mp3", "/B.mp3", false),
                RemoteEntry("文件夹", "/文件夹", true),
                RemoteEntry("a.flac", "/a.flac", false),
            ),
            sourceType = AudioSourceType.WEBDAV,
            connectionId = "nas",
            uriFactory = { "https://nas.local/dav${it.path}" },
        )

        assertThat(tracks.map { it.title }).containsExactly("a", "B").inOrder()
        assertThat(tracks.first().uri).isEqualTo("https://nas.local/dav/a.flac")
    }
}