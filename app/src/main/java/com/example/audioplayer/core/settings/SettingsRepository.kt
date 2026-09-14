package com.example.audioplayer.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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

    val sleepTimerEndAtMillis: Flow<Long?> = context.settingsDataStore.data.map { preferences ->
        preferences[SLEEP_TIMER_END_AT]
    }

    suspend fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[BACKGROUND_PLAYBACK] = enabled }
    }

    suspend fun setSleepTimerEndAt(endAtMillis: Long?) {
        context.settingsDataStore.edit { preferences ->
            if (endAtMillis == null) preferences.remove(SLEEP_TIMER_END_AT)
            else preferences[SLEEP_TIMER_END_AT] = endAtMillis
        }
    }

    private companion object {
        val BACKGROUND_PLAYBACK = booleanPreferencesKey("background_playback")
        val SLEEP_TIMER_END_AT = longPreferencesKey("sleep_timer_end_at")
    }
}