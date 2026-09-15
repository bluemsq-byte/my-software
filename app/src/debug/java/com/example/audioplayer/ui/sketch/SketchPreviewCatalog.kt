package com.example.audioplayer.ui.sketch

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * 16 页草图的 Compose Preview 入口。
 *
 * 预览数据只用于核对布局，不会被接入到生产业务。真实接入时请删除这些默认样例，
 * 由 ViewModel 或状态容器映射成同名的 UI 模型。
 */
private object SketchPreviewData {
    val folders = listOf(
        SketchFolderUiModel("music", "Music", 36),
        SketchFolderUiModel("download", "Download", 18),
        SketchFolderUiModel("family", "Family", 42),
    )

    val tracks = listOf(
        SketchTrackUiModel("1", "Shake It Off", "Taylo Swift", "1989", "03:37", playing = true),
        SketchTrackUiModel("2", "Bad Blood", "Taylo Swift", "1989", "03:30"),
        SketchTrackUiModel("3", "Love Story", "Taylo Swift", "Fearless", "03:55"),
        SketchTrackUiModel("4", "夜曲", "周杰伦", "十一月的萧邦", "03:48"),
    )

    val selectedTracks = tracks.mapIndexed { index, track ->
        track.copy(selected = index < 2)
    }

    val connections = listOf(
        SketchConnectionUiModel("nas", "家里的群晖", "SMB", "已连接"),
        SketchConnectionUiModel("webdav", "WebDAV 音乐", "WebDAV", "上次使用"),
    )

    val shares = listOf(
        SketchFolderUiModel("music-share", "music", 128, "音乐资料库"),
        SketchFolderUiModel("homes-share", "homes", 42, "个人空间"),
        SketchFolderUiModel("video-share", "video", 18, "视频和音频"),
    )

    val playlists = listOf(
        SketchPlaylistUiModel("favorite", "我的收藏", 26, "本地与 NAS"),
        SketchPlaylistUiModel("weekend", "客厅周末", 18, "NAS"),
        SketchPlaylistUiModel("drive", "驾驶歌单", 42, "本地"),
    )

    val devices = listOf(
        SketchDeviceUiModel("phone", "手机扬声器", "本机", SketchDeviceKind.PHONE, selected = true),
        SketchDeviceUiModel("bt", "SoundCore Mini", "蓝牙音响", SketchDeviceKind.BLUETOOTH),
        SketchDeviceUiModel("tv", "客厅电视", "Chromecast / Google Cast", SketchDeviceKind.CAST),
        SketchDeviceUiModel("projector", "客厅投影", "Android 媒体路由", SketchDeviceKind.MEDIA_ROUTE),
    )

    val timers = listOf(
        SketchTimerUiModel(
            id = "morning",
            name = "早晨音乐",
            time = "07:30",
            action = SketchTimerAction.START,
            repeatText = "每天",
            sourceText = "NAS / music / Morning",
            enabled = true,
        ),
        SketchTimerUiModel(
            id = "sleep",
            name = "睡眠停止",
            time = "23:30",
            action = SketchTimerAction.STOP,
            repeatText = "每天",
            sourceText = "当前播放队列",
            enabled = false,
        ),
    )

    val themes = listOf(
        SketchColorTheme.SKY_BLUE.toThemeUiModel(),
        SketchColorTheme.FOREST_GREEN.toThemeUiModel(),
        SketchColorTheme.VIOLET.toThemeUiModel(),
        SketchColorTheme.SUNSET_ORANGE.toThemeUiModel(),
    )

    val menuActions = listOf(
        SketchMenuActionUiModel("立即播放"),
        SketchMenuActionUiModel("下一首播放"),
        SketchMenuActionUiModel("加入播放队列"),
        SketchMenuActionUiModel("加入播放列表"),
        SketchMenuActionUiModel("查看文件信息"),
    )

    val miniPlayer = SketchMiniPlayerUiModel(
        title = "Shake It Off",
        artist = "Taylo Swift",
        isPlaying = true,
    )
}

private fun SketchColorTheme.toThemeUiModel() = SketchThemeUiModel(
    name = displayName,
    startColor = primary,
    endColor = primaryDark,
)

@Preview(name = "01 首次启动与权限", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch01() {
    Sketch01FirstLaunchScreen()
}

@Preview(name = "02 首页本地音乐", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch02() {
    Sketch02LocalLibraryScreen(
        folders = SketchPreviewData.folders,
        songCount = 128,
        miniPlayer = SketchPreviewData.miniPlayer,
    )
}

@Preview(name = "03 首页网络音乐", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch03() {
    Sketch03NetworkLibraryScreen(connections = SketchPreviewData.connections)
}

@Preview(name = "04 添加网络音乐", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch04() {
    Sketch04AddNetworkMusicScreen()
}

@Preview(name = "05 添加连接", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch05() {
    Sketch05ConnectionEditorScreen(
        connectionName = "家里的群晖",
        server = "192.168.2.34",
        username = "maoshiqiang",
        password = "•••••••••",
    )
}

@Preview(name = "06 SMB 共享文件夹", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch06() {
    Sketch06SmbSharePickerScreen(shares = SketchPreviewData.shares)
}

@Preview(name = "07 NAS 文件夹浏览", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch07() {
    Sketch07NasFolderScreen(
        songCount = 13,
        tracks = SketchPreviewData.tracks,
        miniPlayer = SketchPreviewData.miniPlayer,
    )
}

@Preview(name = "08 歌曲三点菜单", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch08() {
    Sketch08TrackMenuScreen(
        songCount = 13,
        track = SketchPreviewData.tracks.first(),
        actions = SketchPreviewData.menuActions,
    )
}

@Preview(name = "09 最近播放", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch09() {
    Sketch09RecentPlayScreen(
        tracks = SketchPreviewData.tracks,
        relativeTimes = mapOf("1" to "刚刚", "2" to "昨天", "3" to "9 月 14 日"),
    )
}

@Preview(name = "10 播放列表", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch10() {
    Sketch10PlaylistListScreen(playlists = SketchPreviewData.playlists)
}

@Preview(name = "11 播放列表多选", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch11() {
    Sketch11PlaylistDetailScreen(
        tracks = SketchPreviewData.selectedTracks,
        selectedCount = 2,
        selecting = true,
    )
}

@Preview(name = "12 播放页", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch12() {
    Sketch12PlayerScreen(
        title = "Shake It Off",
        artist = "Taylo Swift",
        album = "1989",
        deviceName = "客厅音箱",
        isPlaying = true,
        playbackModeIndex = 1,
        queue = SketchPreviewData.tracks,
    )
}

@Preview(name = "13 播放设备", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch13() {
    Sketch13DevicePickerScreen(devices = SketchPreviewData.devices)
}

@Preview(name = "14 定时任务", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch14() {
    Sketch14TimerListScreen(timers = SketchPreviewData.timers)
}

@Preview(name = "15 新建定时", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch15() {
    Sketch15TimerEditorScreen(
        name = "早晨音乐",
        time = "07:30",
        selectedDays = setOf(6),
        source = "NAS / SMB",
        selectedFileOrFolder = "music / Morning",
    )
}

@Preview(name = "16 设置与外观", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PreviewSketch16() {
    Sketch16SettingsScreen(
        themes = SketchPreviewData.themes,
        selectedThemeIndex = 0,
        cacheText = "18.4 MB",
        permissionsText = "本地音乐、通知、精确闹钟",
    )
}
