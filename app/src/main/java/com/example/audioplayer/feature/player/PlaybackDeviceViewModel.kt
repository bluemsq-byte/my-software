package com.example.audioplayer.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.playback.PlaybackDeviceManager
import com.example.audioplayer.core.playback.PlaybackDeviceType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlaybackDeviceViewModel @Inject constructor(
    private val deviceManager: PlaybackDeviceManager,
    private val playbackController: PlaybackController,
) : ViewModel() {
    val devices = deviceManager.devices
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun refresh() = deviceManager.refresh()

    fun select(deviceId: String) {
        viewModelScope.launch {
            val device = devices.value.firstOrNull { it.id == deviceId } ?: return@launch
            val selected = deviceManager.select(deviceId)
            if (!selected) {
                _message.value = "无法切换设备，请使用系统设备选择界面"
                return@launch
            }
            if (device.type == PlaybackDeviceType.CAST) {
                val track = playbackController.currentTrack()
                if (track != null) {
                    deviceManager.playOnCast(track)
                    playbackController.stop()
                    _message.value = "已发送到 Cast 设备"
                } else {
                    _message.value = "请先选择一首歌曲"
                }
            } else {
                _message.value = "已切换播放设备"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}