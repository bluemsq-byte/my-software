# 音频播放器 v1.0.0

安卓本地与群晖 NAS 音频播放器。首页提供本地音乐库和网络音乐/NAS 入口。

## 已实现

- 本地音乐扫描与播放，支持 MP3、WAV、M4A/AAC、FLAC 等系统可解码格式。
- 后台播放、锁屏控制和通知栏控制。
- 同局域网 SMB 和 WebDAV 群晖 NAS 连接。
- NAS 文件夹浏览、在线播放和播放队列。
- 定时开始播放、定时停止、一次或按星期重复。
- 锁屏/熄屏定时执行、开机后恢复定时、精确闹钟权限引导。
- 5/10/15/30/60 分钟倒计时停止。
- NAS 密码使用 Android Keystore 加密保存。
- 设置页、权限状态检查和 NAS 连接管理。

## 安装包

- Release APK：`dist/audio-player-v1.0.0.apk`
- Debug APK：`dist/audio-player-v1.0.0-debug.apk`
- SHA-256：`dist/SHA256SUMS.txt`

Release APK 已使用本项目的本地发布密钥签名。密钥位于 `.secrets/audio-player-release.jks`，签名配置位于
`keystore.properties`。这两个文件已被 Git 忽略，请妥善备份；后续升级必须继续使用同一个密钥。

## 开发环境

- JDK 17
- Android SDK 35
- Gradle Wrapper 8.14.3
- Kotlin 2.2.0
- Jetpack Compose、Media3、Room、Hilt、OkHttp、SMBJ

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

- 第一版只连接同一局域网内的 NAS，不包含 QuickConnect 或互联网远程访问。
- NAS 音乐只在线播放，不下载和离线缓存。
- 不接入在线音乐平台、网盘、歌词、音效和跨设备同步。
- 群晖需启用 SMB2/SMB3 或 WebDAV，并提供可访问的共享文件夹。
- WebDAV 支持 HTTP 和有效证书的 HTTPS；自签名证书不默认信任。
