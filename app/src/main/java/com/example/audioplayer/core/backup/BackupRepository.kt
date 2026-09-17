package com.example.audioplayer.core.backup

import com.example.audioplayer.core.database.ConnectionDao
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.database.PlaylistDao
import com.example.audioplayer.core.database.PlaylistEntity
import com.example.audioplayer.core.database.PlaylistItemEntity
import com.example.audioplayer.core.database.TimerDao
import com.example.audioplayer.core.database.TimerEntity
import com.example.audioplayer.core.database.TimerFileDao
import com.example.audioplayer.core.database.TimerFileEntity
import com.example.audioplayer.core.settings.AppThemeColor
import com.example.audioplayer.core.settings.DarkModeSetting
import com.example.audioplayer.core.settings.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * 轻量备份只保存连接元数据、播放列表、定时和主题设置，不保存密码。
 */
@Singleton
class BackupRepository @Inject constructor(
    private val connectionDao: ConnectionDao,
    private val playlistDao: PlaylistDao,
    private val timerDao: TimerDao,
    private val timerFileDao: TimerFileDao,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun exportJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("connections", connectionDao.observeAll().first().toJsonArray())
        root.put("playlists", exportPlaylists())
        root.put("timers", exportTimers())
        root.put("settings", JSONObject().apply {
            put("themeColor", settingsRepository.themeColor.first().name)
            put("darkMode", settingsRepository.darkModeSetting.first().name)
            put("backgroundPlayback", settingsRepository.backgroundPlaybackEnabled.first())
            put(
                "downloadNetworkOnPlaylistAdd",
                settingsRepository.downloadNetworkOnPlaylistAdd.first(),
            )
            put("networkMusicAudioOnly", settingsRepository.networkMusicAudioOnly.first())
        })
        return root.toString(2)
    }

    suspend fun importJson(json: String): String {
        val root = JSONObject(json)
        require(root.optInt("version", 0) == 1) { "不支持的备份版本" }
        root.optJSONArray("connections")?.let { items -> importConnections(items) }
        root.optJSONArray("playlists")?.let { items -> importPlaylists(items) }
        root.optJSONArray("timers")?.let { items -> importTimers(items) }
        root.optJSONObject("settings")?.let { settings ->
            runCatching {
                AppThemeColor.valueOf(settings.optString("themeColor"))
            }.getOrNull()?.let { settingsRepository.setThemeColor(it) }
            runCatching {
                DarkModeSetting.valueOf(settings.optString("darkMode"))
            }.getOrNull()?.let { settingsRepository.setDarkModeSetting(it) }
            if (settings.has("backgroundPlayback")) {
                settingsRepository.setBackgroundPlaybackEnabled(
                    settings.optBoolean("backgroundPlayback", true),
                )
            }
            if (settings.has("downloadNetworkOnPlaylistAdd")) {
                settingsRepository.setDownloadNetworkOnPlaylistAdd(
                    settings.optBoolean("downloadNetworkOnPlaylistAdd", false),
                )
            }
            if (settings.has("networkMusicAudioOnly")) {
                settingsRepository.setNetworkMusicAudioOnly(
                    settings.optBoolean("networkMusicAudioOnly", false),
                )
            }
        }
        return "备份已恢复，NAS 密码需要重新填写"
    }

    private suspend fun exportPlaylists(): JSONArray {
        val result = JSONArray()
        playlistDao.observeSummaries().first().forEach { summary ->
            val items = playlistDao.getItems(summary.id)
            result.put(JSONObject().apply {
                put("name", summary.name)
                put("items", JSONArray().apply {
                    items.forEach { item ->
                        put(JSONObject().apply {
                            put("mediaId", item.mediaId)
                            put("title", item.title)
                            put("artist", item.artist)
                            put("album", item.album)
                            put("durationMillis", item.durationMillis)
                            put("uri", item.uri)
                            put("artworkUri", item.artworkUri)
                            put("sourceType", item.sourceType.name)
                            put("connectionId", item.connectionId)
                            put("remotePath", item.remotePath)
                        })
                    }
                })
            })
        }
        return result
    }

    private suspend fun exportTimers(): JSONArray {
        val result = JSONArray()
        timerDao.observeAll().first().forEach { timer ->
            result.put(JSONObject().apply {
                put("name", timer.name)
                put("action", timer.action.name)
                put("hour", timer.hour)
                put("minute", timer.minute)
                put("repeatDaysMask", timer.repeatDaysMask)
                put("enabled", timer.enabled)
                put("sourceType", timer.sourceType?.name)
                put("selectionType", timer.selectionType.name)
                put("connectionId", timer.connectionId)
                put("sourcePath", timer.sourcePath)
                put(
                    "files",
                    JSONArray(timerFileDao.getForTimer(timer.id).map(TimerFileEntity::path)),
                )
            })
        }
        return result
    }

    private suspend fun importConnections(items: JSONArray) {
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            connectionDao.upsert(
                ConnectionEntity(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    protocol = com.example.audioplayer.core.model.ConnectionProtocol.valueOf(
                        item.optString("protocol"),
                    ),
                    host = item.optString("host"),
                    port = item.optInt("port").takeIf { it > 0 },
                    username = item.optString("username"),
                    share = item.optString("share").ifBlank { null },
                    selectedShare = item.optString("selectedShare").ifBlank { null },
                    basePath = item.optString("basePath").ifBlank { "/" },
                    domain = item.optString("domain").ifBlank { null },
                    useHttps = item.optBoolean("useHttps", true),
                ),
            )
        }
    }

    private suspend fun importPlaylists(items: JSONArray) {
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            val now = System.currentTimeMillis()
            val playlistId = playlistDao.upsertPlaylist(
                PlaylistEntity(
                    name = item.optString("name"),
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                ),
            )
            val itemArray = item.optJSONArray("items") ?: continue
            val entities = buildList {
                for (itemIndex in 0 until itemArray.length()) {
                    val track = itemArray.getJSONObject(itemIndex)
                    add(
                        PlaylistItemEntity(
                            playlistId = playlistId,
                            position = itemIndex,
                            mediaId = track.optString("mediaId"),
                            title = track.optString("title"),
                            artist = track.optString("artist").ifBlank { null },
                            album = track.optString("album").ifBlank { null },
                            durationMillis = track.optLong("durationMillis"),
                            uri = track.optString("uri"),
                            artworkUri = track.optString("artworkUri").ifBlank { null },
                            sourceType = com.example.audioplayer.core.model.AudioSourceType.valueOf(
                                track.optString("sourceType"),
                            ),
                            connectionId = track.optString("connectionId").ifBlank { null },
                            remotePath = track.optString("remotePath").ifBlank { null },
                        ),
                    )
                }
            }
            playlistDao.insertItems(entities)
        }
    }

    private suspend fun importTimers(items: JSONArray) {
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            val timerId = timerDao.upsert(
                TimerEntity(
                    name = item.optString("name"),
                    action = com.example.audioplayer.core.model.TimerAction.valueOf(
                        item.optString("action"),
                    ),
                    hour = item.optInt("hour"),
                    minute = item.optInt("minute"),
                    repeatDaysMask = item.optInt("repeatDaysMask"),
                    enabled = item.optBoolean("enabled"),
                    sourceType = item.optString("sourceType").ifBlank { null }?.let {
                        com.example.audioplayer.core.model.TimerSourceType.valueOf(it)
                    },
                    selectionType = com.example.audioplayer.core.model.TimerSelectionType.valueOf(
                        item.optString("selectionType"),
                    ),
                    connectionId = item.optString("connectionId").ifBlank { null },
                    sourcePath = item.optString("sourcePath").ifBlank { null },
                    createdAtEpochMillis = System.currentTimeMillis(),
                    lastRunEpochMillis = null,
                ),
            )
            val files = item.optJSONArray("files") ?: continue
            timerFileDao.insertAll(
                buildList {
                    for (fileIndex in 0 until files.length()) {
                        add(
                            TimerFileEntity(
                                timerId = timerId,
                                position = fileIndex,
                                path = files.getString(fileIndex),
                            ),
                        )
                    }
                },
            )
        }
    }

    private fun List<ConnectionEntity>.toJsonArray(): JSONArray = JSONArray().apply {
        this@toJsonArray.forEach { connection ->
            put(JSONObject().apply {
                put("id", connection.id)
                put("name", connection.name)
                put("protocol", connection.protocol.name)
                put("host", connection.host)
                put("port", connection.port)
                put("username", connection.username)
                put("share", connection.share)
                put("selectedShare", connection.selectedShare)
                put("basePath", connection.basePath)
                put("domain", connection.domain)
                put("useHttps", connection.useHttps)
            })
        }
    }
}
