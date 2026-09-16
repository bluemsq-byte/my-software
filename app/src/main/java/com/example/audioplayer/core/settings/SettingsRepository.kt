package com.example.audioplayer.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val backgroundPlaybackEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[BACKGROUND_PLAYBACK] ?: true
    }

    val darkModeSetting: Flow<DarkModeSetting> = context.settingsDataStore.data.map { preferences ->
        runCatching {
            DarkModeSetting.valueOf(preferences[DARK_MODE] ?: DarkModeSetting.SYSTEM.name)
        }.getOrDefault(DarkModeSetting.SYSTEM)
    }

    val themeColor: Flow<AppThemeColor> = context.settingsDataStore.data.map { preferences ->
        runCatching {
            AppThemeColor.valueOf(preferences[THEME_COLOR] ?: AppThemeColor.SKY_BLUE.name)
        }.getOrDefault(AppThemeColor.SKY_BLUE)
    }

    val sleepTimerEndAtMillis: Flow<Long?> = context.settingsDataStore.data.map { preferences ->
        preferences[SLEEP_TIMER_END_AT]
    }

    val downloadNetworkOnPlaylistAdd: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[DOWNLOAD_NETWORK_ON_PLAYLIST_ADD] ?: false
    }

    fun lastRemotePath(connectionId: String): Flow<String?> =
        context.settingsDataStore.data.map { preferences ->
            preferences[lastRemotePathKey(connectionId)]
        }

    suspend fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[BACKGROUND_PLAYBACK] = enabled }
    }

    suspend fun setDarkModeSetting(setting: DarkModeSetting) {
        context.settingsDataStore.edit { it[DARK_MODE] = setting.name }
    }

    suspend fun setThemeColor(color: AppThemeColor) {
        context.settingsDataStore.edit { it[THEME_COLOR] = color.name }
    }

    suspend fun setSleepTimerEndAt(endAtMillis: Long?) {
        context.settingsDataStore.edit { preferences ->
            if (endAtMillis == null) preferences.remove(SLEEP_TIMER_END_AT)
            else preferences[SLEEP_TIMER_END_AT] = endAtMillis
        }
    }

    suspend fun setLastRemotePath(connectionId: String, path: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[lastRemotePathKey(connectionId)] = path
        }
    }

    suspend fun setDownloadNetworkOnPlaylistAdd(enabled: Boolean) {
        context.settingsDataStore.edit {
            it[DOWNLOAD_NETWORK_ON_PLAYLIST_ADD] = enabled
        }
    }

    private fun lastRemotePathKey(connectionId: String) =
        stringPreferencesKey("last_remote_path_$connectionId")

    private companion object {
        val BACKGROUND_PLAYBACK = booleanPreferencesKey("background_playback")
        val SLEEP_TIMER_END_AT = longPreferencesKey("sleep_timer_end_at")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val THEME_COLOR = stringPreferencesKey("theme_color")
        val DOWNLOAD_NETWORK_ON_PLAYLIST_ADD =
            booleanPreferencesKey("download_network_on_playlist_add")
    }
}
