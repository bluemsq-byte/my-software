package com.example.audioplayer.core.playback

import androidx.media3.common.Player

object PlaybackModeMapper {
    fun toRepeatMode(mode: PlaybackMode): Int = when (mode) {
        PlaybackMode.SEQUENTIAL -> Player.REPEAT_MODE_OFF
        PlaybackMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
        PlaybackMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
    }

    fun fromRepeatMode(repeatMode: Int): PlaybackMode = when (repeatMode) {
        Player.REPEAT_MODE_ONE -> PlaybackMode.REPEAT_ONE
        Player.REPEAT_MODE_ALL -> PlaybackMode.REPEAT_ALL
        else -> PlaybackMode.SEQUENTIAL
    }
}