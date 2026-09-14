package com.example.audioplayer.core.playback

import androidx.media3.common.Player
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaybackModeMapperTest {
    @Test
    fun mapsAllPlaybackModesBothDirections() {
        assertThat(PlaybackModeMapper.toRepeatMode(PlaybackMode.SEQUENTIAL)).isEqualTo(Player.REPEAT_MODE_OFF)
        assertThat(PlaybackModeMapper.toRepeatMode(PlaybackMode.REPEAT_ALL)).isEqualTo(Player.REPEAT_MODE_ALL)
        assertThat(PlaybackModeMapper.toRepeatMode(PlaybackMode.REPEAT_ONE)).isEqualTo(Player.REPEAT_MODE_ONE)
        assertThat(PlaybackModeMapper.fromRepeatMode(Player.REPEAT_MODE_OFF)).isEqualTo(PlaybackMode.SEQUENTIAL)
        assertThat(PlaybackModeMapper.fromRepeatMode(Player.REPEAT_MODE_ALL)).isEqualTo(PlaybackMode.REPEAT_ALL)
        assertThat(PlaybackModeMapper.fromRepeatMode(Player.REPEAT_MODE_ONE)).isEqualTo(PlaybackMode.REPEAT_ONE)
    }
}