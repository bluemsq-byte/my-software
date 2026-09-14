# 音频播放器 v1.5.0

安卓本地与群晖 NAS 音频播放器。首页提供本地音乐库、网络音乐、最近播放和播放列表入口。

## 已实现

- 本地音乐扫描与播放，支持 MP3、WAV、M4A/AAC、FLAC 等系统可解码格式。
- 后台播放、锁屏控制和通知栏控制。
- 同局域网 SMB 和 WebDAV 群晖 NAS 连接。
- SMB 连接后自动列出可访问的共享文件夹，并记住上次选择。
- 网络连接支持测试、编辑、删除和重新连接。
- 本地音乐检索和当前 NAS 文件夹检索。
- 最近播放记录和清空功能。
- 定时开始播放、定时停止、一次或按星期重复。
- 定时任务支持选择文件或整个文件夹，星期选择包含周日。
- 播放页支持顺序播放、列表循环、单曲循环和播放设备选择。
- 播放设备支持蓝牙、Android 媒体路由和 Chromecast/Google Cast。
- 本地和 NAS 音乐可通过局域网代理发送到 Chromecast。
- 播放列表支持添加网络歌曲。
- 设置页支持自动深色模式、浅色/深色切换和六种颜色主题。
- 锁屏/熄屏定时执行、开机后恢复定时、精确闹钟权限引导。
- 5/10/15/30/60 分钟倒计时停止。
- 播放列表：新建、添加本地或 NAS 歌曲、移除、排序和播放全部。
- 设置页支持缓存统计与清理。
- 设置页不再重复管理 NAS 和蓝牙，相关功能统一放在网络音乐页和播放页。
- NAS 密码使用 Android Keystore 加密保存。

## 安装包

- Release APK：`dist/audio-player-v1.5.0.apk`
- Debug APK：`dist/audio-player-v1.5.0-debug.apk`
- SHA-256：`dist/SHA256SUMS.txt`

Release APK 已使用本项目的本地发布密钥签名。密钥位于 `.secrets/audio-player-release.jks`，签名配置位于
`keystore.properties`。这两个文件已被 Git 忽略，请妥善备份；后续升级必须继续使用同一个密钥。

## 开发环境

- JDK 17
- Android SDK 35
- Gradle Wrapper 8.14.3
- Kotlin 2.2.0
- Jetpack Compose、Media3、Room、Hilt、OkHttp、SMBJ、jCIFS-ng、MediaRouter

建议将项目放在纯 ASCII 路径下开发。在 Windows 中文路径下，项目已启用
`android.overridePathCheck=true`，但 Java 测试工作进程可能仍受路径编码影响。

## 构建和测试

```powershell
.\gradlew.bat test
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

## 需求边界

- 只连接同一局域网内的 NAS，不包含 QuickConnect 或互联网远程访问。
- NAS 音乐只在线播放，不下载和离线缓存。
- 网络音乐只检索当前 NAS 文件夹，不递归搜索子文件夹。
- 蓝牙切换受手机厂商和系统限制；应用会尝试切换，失败时提示使用系统蓝牙设置。
- 清除缓存不会删除音乐、连接、密码、定时、播放列表或最近播放记录。
- WebDAV 支持 HTTP 和有效证书的 HTTPS；自签名证书不默认信任。
