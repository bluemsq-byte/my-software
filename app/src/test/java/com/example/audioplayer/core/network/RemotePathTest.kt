package com.example.audioplayer.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RemotePathTest {
    @Test
    fun normalize_removesTraversalAndDuplicateSeparators() {
        assertThat(RemotePath.normalize("//music\\album/../song.mp3")).isEqualTo("/music/song.mp3")
        assertThat(RemotePath.normalize("../../music")).isEqualTo("/music")
        assertThat(RemotePath.normalize("")).isEqualTo("/")
    }

    @Test
    fun joinAndParent_keepRootSafe() {
        assertThat(RemotePath.join("/music", "华语/歌曲.mp3")).isEqualTo("/music/华语/歌曲.mp3")
        assertThat(RemotePath.parent("/music/华语/歌曲.mp3")).isEqualTo("/music/华语")
        assertThat(RemotePath.parent("/")).isNull()
    }

    @Test
    fun smbConversion_usesBackslashesWithoutLeadingSeparator() {
        assertThat(RemotePath.toSmb("/music/album/song.mp3")).isEqualTo("music\\album\\song.mp3")
        assertThat(RemotePath.fromSmb("music\\album\\song.mp3")).isEqualTo("/music/album/song.mp3")
    }
}