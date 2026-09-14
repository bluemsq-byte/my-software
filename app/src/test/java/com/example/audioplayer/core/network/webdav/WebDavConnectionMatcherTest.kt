package com.example.audioplayer.core.network.webdav

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WebDavConnectionMatcherTest {
    @Test
    fun matches_acceptsSameHostPortAndBasePath() {
        assertThat(
            WebDavConnectionMatcher.matches(
                "https://nas.local:5006/dav/music",
                "https://nas.local:5006/dav/music/album/song.mp3",
            ),
        ).isTrue()
    }

    @Test
    fun matches_rejectsDifferentPortOrBasePath() {
        assertThat(
            WebDavConnectionMatcher.matches(
                "https://nas.local:5006/dav/music",
                "https://nas.local:5005/dav/music/song.mp3",
            ),
        ).isFalse()
        assertThat(
            WebDavConnectionMatcher.matches(
                "https://nas.local:5006/dav/music",
                "https://nas.local:5006/other/song.mp3",
            ),
        ).isFalse()
    }
}