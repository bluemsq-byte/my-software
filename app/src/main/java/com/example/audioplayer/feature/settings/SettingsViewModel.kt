package com.example.audioplayer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.settings.CacheManager
import com.example.audioplayer.core.settings.CacheUsage
import com.example.audioplayer.core.settings.SettingsRepository
import com.example.audioplayer.core.storage.LocalMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val settingsRepository: SettingsRepository,
    private val localMediaRepository: LocalMediaRepository,
    private val cacheManager: CacheManager,
    val bluetoothRouteManager: BluetoothRouteManager,
) : ViewModel() {
    val connections: StateFlow<List<ConnectionEntity>> = connectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _cacheUsage = MutableStateFlow(CacheUsage(0L, 0L))
    val cacheUsage: StateFlow<CacheUsage> = _cacheUsage.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val bluetoothDevices = bluetoothRouteManager.devices

    init {
        refreshCacheUsage()
        bluetoothRouteManager.refresh()
    }

    val backgroundPlaybackEnabled: StateFlow<Boolean> = settingsRepository.backgroundPlaybackEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackgroundPlaybackEnabled(enabled)
        }
    }

    fun deleteConnection(connection: ConnectionEntity) {
        viewModelScope.launch {
            connectionRepository.get(connection.id)?.let { connectionRepository.delete(it) }
        }
    }

    fun refreshCacheUsage() {
        viewModelScope.launch {
            _cacheUsage.value = cacheManager.usage()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            cacheManager.clear()
            _cacheUsage.value = cacheManager.usage()
            _message.value = "缓存已清理"
        }
    }

    fun selectBluetoothDevice(deviceId: String) {
        _message.value = if (bluetoothRouteManager.select(deviceId)) {
            "已切换蓝牙输出"
        } else {
            "无法切换该设备，请在系统蓝牙设置中选择"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun rescanLocalMusic() {
        viewModelScope.launch {
            runCatching { localMediaRepository.scan() }
        }
    }
}