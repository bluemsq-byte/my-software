package com.example.audioplayer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.settings.AppThemeColor
import com.example.audioplayer.core.settings.CacheManager
import com.example.audioplayer.core.settings.CacheUsage
import com.example.audioplayer.core.settings.DarkModeSetting
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
    private val settingsRepository: SettingsRepository,
    private val localMediaRepository: LocalMediaRepository,
    private val cacheManager: CacheManager,
) : ViewModel() {
    private val _cacheUsage = MutableStateFlow(CacheUsage(0L, 0L))
    val cacheUsage: StateFlow<CacheUsage> = _cacheUsage.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val backgroundPlaybackEnabled: StateFlow<Boolean> = settingsRepository.backgroundPlaybackEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val darkModeSetting: StateFlow<DarkModeSetting> = settingsRepository.darkModeSetting
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DarkModeSetting.SYSTEM)
    val themeColor: StateFlow<AppThemeColor> = settingsRepository.themeColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemeColor.SKY_BLUE)

    init {
        refreshCacheUsage()
    }

    fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBackgroundPlaybackEnabled(enabled) }
    }

    fun setDarkModeSetting(setting: DarkModeSetting) {
        viewModelScope.launch { settingsRepository.setDarkModeSetting(setting) }
    }

    fun setThemeColor(color: AppThemeColor) {
        viewModelScope.launch { settingsRepository.setThemeColor(color) }
    }

    fun refreshCacheUsage() {
        viewModelScope.launch { _cacheUsage.value = cacheManager.usage() }
    }

    fun clearCache() {
        viewModelScope.launch {
            cacheManager.clear()
            _cacheUsage.value = cacheManager.usage()
            _message.value = "缓存已清理"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun rescanLocalMusic() {
        viewModelScope.launch { runCatching { localMediaRepository.scan() } }
    }
}