package com.example.audioplayer.ui.sketch

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 以下模型只服务 UI 草图和 Compose Preview。
 * 接入真实业务时，在页面边界把 Room、Media3 或 NAS 模型映射成这些字段即可。
 */

@Immutable
data class SketchFolderUiModel(
    val id: String,
    val name: String,
    val songCount: Int,
    val description: String? = null,
)

@Immutable
data class SketchTrackUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val selected: Boolean = false,
    val playing: Boolean = false,
)

@Immutable
data class SketchConnectionUiModel(
    val id: String,
    val name: String,
    val protocol: String,
    val status: String,
    val selected: Boolean = false,
)

@Immutable
data class SketchPlaylistUiModel(
    val id: String,
    val name: String,
    val songCount: Int,
    val source: String,
)

@Immutable
data class SketchMiniPlayerUiModel(
    val title: String,
    val artist: String,
    val isPlaying: Boolean,
)

enum class SketchDeviceKind {
    PHONE,
    BLUETOOTH,
    CAST,
    MEDIA_ROUTE,
}

@Immutable
data class SketchDeviceUiModel(
    val id: String,
    val name: String,
    val description: String,
    val kind: SketchDeviceKind,
    val selected: Boolean = false,
)

enum class SketchTimerAction {
    START,
    STOP,
}

@Immutable
data class SketchTimerUiModel(
    val id: String,
    val name: String,
    val time: String,
    val action: SketchTimerAction,
    val repeatText: String,
    val sourceText: String,
    val enabled: Boolean,
)

@Immutable
data class SketchThemeUiModel(
    val name: String,
    val startColor: Color,
    val endColor: Color,
)

@Immutable
data class SketchNavItemUiModel(
    val label: String,
)

@Immutable
data class SketchMenuActionUiModel(
    val label: String,
)
