# Kotlin + Jetpack Compose 草图 UI 接入说明

## 代码位置

- 设计令牌：`app/src/main/java/com/example/audioplayer/ui/sketch/SketchDesignTokens.kt`
- UI 模型：`app/src/main/java/com/example/audioplayer/ui/sketch/SketchUiModels.kt`
- 公共组件：`app/src/main/java/com/example/audioplayer/ui/sketch/SketchComponents.kt`
- 16 页布局：`app/src/main/java/com/example/audioplayer/ui/sketch/Sketch*Screens.kt`
- Compose Preview：`app/src/debug/java/com/example/audioplayer/ui/sketch/SketchPreviewCatalog.kt`

## 页面映射

| 草图 | Compose 函数 |
| --- | --- |
| 01 首次启动与权限 | `Sketch01FirstLaunchScreen` |
| 02 首页本地音乐 | `Sketch02LocalLibraryScreen` |
| 03 首页网络音乐 | `Sketch03NetworkLibraryScreen` |
| 04 添加网络音乐 | `Sketch04AddNetworkMusicScreen` |
| 05 添加/编辑连接 | `Sketch05ConnectionEditorScreen` |
| 06 SMB 共享文件夹 | `Sketch06SmbSharePickerScreen` |
| 07 NAS 文件夹浏览 | `Sketch07NasFolderScreen` |
| 08 歌曲三点菜单 | `Sketch08TrackMenuScreen` |
| 09 最近播放 | `Sketch09RecentPlayScreen` |
| 10 播放列表 | `Sketch10PlaylistListScreen` |
| 11 播放列表详情与多选 | `Sketch11PlaylistDetailScreen` |
| 12 播放页 | `Sketch12PlayerScreen` |
| 13 播放设备选择 | `Sketch13DevicePickerScreen` |
| 14 定时任务列表 | `Sketch14TimerListScreen` |
| 15 新建定时 | `Sketch15TimerEditorScreen` |
| 16 设置与外观 | `Sketch16SettingsScreen` |

## 接入原则

1. 页面函数只接收 UI 模型和回调，不直接访问 Room、Media3、SMB 或 WebDAV。
2. 回调默认是空函数，用于确认布局；接入时替换为 NavController、ViewModel 或播放控制器调用。
3. 颜色、字号、间距、圆角全部从 `SketchDesignTokens.kt` 读取。
4. 公共按钮、搜索框、列表行、底部导航、迷你播放器和播放控制都放在
   `SketchComponents.kt`，页面不重复实现。
5. `SketchPreviewCatalog.kt` 仅位于 debug source set，不会进入 Release APK。

## 推荐接入方式

先在现有 Navigation 路由中替换单个页面，例如：

```kotlin
composable(ROUTE_LIBRARY) {
    Sketch02LocalLibraryScreen(
        query = state.query,
        folders = state.folders.map { folder ->
            SketchFolderUiModel(
                id = folder.id,
                name = folder.name,
                songCount = folder.songCount,
            )
        },
        songCount = state.songCount,
        onQueryChange = viewModel::onQueryChange,
        onFolderClick = { viewModel.openFolder(it.id) },
    )
}
```

确认单页布局和功能后，再逐页替换。不要一次性接入全部页面，避免业务状态和
导航返回栈同时变化导致难以定位问题。
