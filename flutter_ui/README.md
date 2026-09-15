# Flutter UI 工程说明

本目录是 Android 应用的 Flutter UI 对照实现，结构对应
`design/sketches.html` 和 `PRODUCT_DESIGN_SPEC.md`。

## 目录

```text
lib/
  design/
    app_theme.dart
    design_tokens.dart
    glass_components.dart
  models/
    ui_models.dart
  screens/
    home_screens.dart
    library_screens.dart
    network_screens.dart
    player_screens.dart
    timer_settings_screens.dart
  widgets/
    app_shell.dart
    track_widgets.dart
  main.dart
```

## 页面清单

- 首次启动与权限：`PermissionScreen`
- 首页本地音乐：`HomeLocalScreen`
- 首页网络音乐：`HomeNetworkScreen`
- 添加网络音乐：`AddNetworkMusicScreen`
- 添加或编辑网络连接：`ConnectionEditorScreen`
- SMB 共享文件夹选择：`SmbSharePickerScreen`
- NAS 文件夹浏览：`BrowserScreen`
- 歌曲三点菜单：`TrackMenuPreviewScreen`
- 最近播放：`RecentScreen`
- 播放列表列表：`PlaylistListScreen`
- 播放列表详情和多选：`PlaylistDetailScreen`
- DS Audio 风格播放页：`PlayerScreen`
- 播放设备选择：`DevicePickerScreen`
- 定时任务：`TimerListScreen`
- 新建定时：`TimerEditorScreen`
- 设置和外观：`SettingsScreen`

## 公共组件

- `GlassBackground`
- `GlassCard`
- `GlassTrackRow`
- `GlassFolderRow`
- `GlassMiniPlayer`
- `GlassBottomNav`
- `AppPrimaryButton`
- `AppSecondaryButton`
- `AppIconButton`

## 说明

- 当前代码只负责 UI 布局。
- 播放、网络连接、权限、定时器、队列排序等位置都使用 `TODO` 注释预留。
- 所有页面颜色、字号、间距和圆角都从 `design_tokens.dart` 读取。
- 草图使用占位封面和模拟数据，后续替换成真实模型即可。
