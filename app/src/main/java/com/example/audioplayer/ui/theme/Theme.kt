package com.example.audioplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.audioplayer.core.settings.AppThemeColor

private data class ThemePalette(
    val lightPrimary: Color,
    val lightSecondary: Color,
    val darkPrimary: Color,
    val darkSecondary: Color,
)

private fun palette(theme: AppThemeColor): ThemePalette = when (theme) {
    AppThemeColor.SKY_BLUE -> ThemePalette(Color(0xFF1565C0), Color(0xFF00838F), Color(0xFF90CAF9), Color(0xFF80DEEA))
    AppThemeColor.FOREST_GREEN -> ThemePalette(Color(0xFF2E7D32), Color(0xFF558B2F), Color(0xFFA5D6A7), Color(0xFFC5E1A5))
    AppThemeColor.VIOLET -> ThemePalette(Color(0xFF6A1B9A), Color(0xFF7B1FA2), Color(0xFFCE93D8), Color(0xFFE1BEE7))
    AppThemeColor.SUNSET_ORANGE -> ThemePalette(Color(0xFFE65100), Color(0xFFBF360C), Color(0xFFFFB74D), Color(0xFFFF8A65))
    AppThemeColor.SAKURA_PINK -> ThemePalette(Color(0xFFAD1457), Color(0xFFC2185B), Color(0xFFF48FB1), Color(0xFFF8BBD0))
    AppThemeColor.TEAL -> ThemePalette(Color(0xFF00695C), Color(0xFF00796B), Color(0xFF80CBC4), Color(0xFFB2DFDB))
}

@Composable
fun AudioPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: AppThemeColor = AppThemeColor.SKY_BLUE,
    content: @Composable () -> Unit,
) {
    val palette = palette(themeColor)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.darkPrimary,
            secondary = palette.darkSecondary,
        )
    } else {
        lightColorScheme(
            primary = palette.lightPrimary,
            secondary = palette.lightSecondary,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}