package com.example.audioplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.ui.AudioPlayerApp
import com.example.audioplayer.ui.theme.AudioPlayerTheme
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.settings.DarkModeSetting
import com.example.audioplayer.core.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var playbackController: PlaybackController
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModeSetting by settingsRepository.darkModeSetting.collectAsStateWithLifecycle(
                initialValue = DarkModeSetting.SYSTEM,
            )
            val themeColor by settingsRepository.themeColor.collectAsStateWithLifecycle(
                initialValue = com.example.audioplayer.core.settings.AppThemeColor.SKY_BLUE,
            )
            val darkTheme = when (darkModeSetting) {
                DarkModeSetting.SYSTEM -> isSystemInDarkTheme()
                DarkModeSetting.LIGHT -> false
                DarkModeSetting.DARK -> true
                else -> isSystemInDarkTheme()
            }
            AudioPlayerTheme(
                darkTheme = darkTheme,
                themeColor = themeColor,
            ) {
                AudioPlayerApp(playbackController)
            }
        }
    }
}