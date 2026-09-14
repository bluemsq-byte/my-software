package com.example.audioplayer.core.network.smb

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RemoteSmbPathTest {
    @Test
    fun fromRemote_convertsAbsoluteRemotePaths() {
        assertThat(RemoteSmbPath.fromRemote("/音乐/歌曲.mp3")).isEqualTo("音乐\\歌曲.mp3")
        assertThat(RemoteSmbPath.fromRemote("/")).isEmpty()
    }

    @Test
    fun join_buildsStableWindowsStylePaths() {
        assertThat(RemoteSmbPath.join("/music", "album")).isEqualTo("\\music\\album")
        assertThat(RemoteSmbPath.join("\\music", "song.mp3")).isEqualTo("\\music\\song.mp3")
    }
}