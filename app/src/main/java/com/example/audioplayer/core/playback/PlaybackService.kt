@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.audioplayer.MainActivity
import com.example.audioplayer.R
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSelectionType
import com.example.audioplayer.core.model.TimerSourceType
import com.example.audioplayer.core.repository.RemoteFileRepository
import com.example.audioplayer.core.repository.TimerRepository
import com.example.audioplayer.core.settings.SettingsRepository
import com.example.audioplayer.core.storage.LocalMediaRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject lateinit var dataSourceFactory: AppDataSourceFactory
    @Inject lateinit var timerRepository: TimerRepository
    @Inject lateinit var remoteFileRepository: RemoteFileRepository
    @Inject lateinit var localMediaRepository: LocalMediaRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var sleepTimerRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        val exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player = exoPlayer

        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivity)
            .build()

        serviceScope.launch {
            settingsRepository.sleepTimerEndAtMillis.first()?.let(::scheduleSleepTimer)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val superResult = super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_TIMER -> {
                promoteToForegroundForPreparation()
                val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
                if (timerId > 0L) {
                    serviceScope.launch { startTimer(timerId) }
                }
            }

            ACTION_STOP_TIMER -> player?.pause()
            ACTION_SET_SLEEP_TIMER -> {
                promoteToForegroundForPreparation()
                val endAtMillis = intent.getLongExtra(EXTRA_SLEEP_TIMER_END_AT, 0L)
                if (endAtMillis > 0L) {
                    serviceScope.launch {
                        settingsRepository.setSleepTimerEndAt(endAtMillis)
                        scheduleSleepTimer(endAtMillis)
                    }
                }
            }

            ACTION_CANCEL_SLEEP_TIMER -> {
                serviceScope.launch { settingsRepository.setSleepTimerEndAt(null) }
                cancelSleepTimer()
            }
        }
        return superResult
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        serviceScope.launch {
            if (!settingsRepository.backgroundPlaybackEnabled.first()) {
                player?.pause()
                stopSelf()
            }
        }
        super.onTaskRemoved(rootIntent)
    }


    override fun onDestroy() {
        cancelSleepTimer()
        serviceScope.cancel()
        player?.release()
        player = null
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private suspend fun startTimer(timerId: Long) {
        val task = timerRepository.get(timerId) ?: return
        if (task.action != TimerAction.START || task.sourceType == null) return

        val tracks = runCatching { loadTimerTracks(task) }.getOrElse { error ->
            showTimerError("定时播放失败：${error.message ?: "无法读取音乐"}")
            return
        }

        if (tracks.isEmpty()) {
            showTimerError("定时播放失败：指定目录没有可播放音乐")
            return
        }
        playTracks(tracks)
    }

    private suspend fun loadTimerTracks(task: com.example.audioplayer.core.model.TimerTask): List<AudioTrack> {
        val sourceType = task.sourceType ?: throw IllegalStateException("缺少音乐来源")
        return when (sourceType) {
            TimerSourceType.LOCAL_FOLDER -> {
                val localTracks = localMediaRepository.scan()
                if (task.selectionType == TimerSelectionType.FILES) {
                    val selected = task.selectedFiles.toSet()
                    task.selectedFiles.mapNotNull { uri -> localTracks.firstOrNull { it.uri == uri } }
                } else {
                    localTracks
                        .filter { it.remotePath == task.sourcePath }
                        .sortedBy { it.title.lowercase() }
                }
            }

            TimerSourceType.SMB_FOLDER,
            TimerSourceType.WEBDAV_FOLDER,
            -> {
                val connectionId = task.connectionId ?: run {
                    throw IllegalStateException("缺少 NAS 连接")
                }
                val path = task.sourcePath ?: "/"
                repeat(2) { attempt ->
                    try {
                        val entries = remoteFileRepository.list(connectionId, path)
                        val queue = remoteFileRepository.buildQueue(connectionId, path, entries)
                        return if (task.selectionType == TimerSelectionType.FILES) {
                            val queueByPath = queue.associateBy { it.remotePath }
                            task.selectedFiles.mapNotNull(queueByPath::get)
                        } else {
                            queue
                        }
                    } catch (error: Exception) {
                        if (attempt == 1) throw error
                        delay(1_500L)
                    }
                }
                emptyList()
            }
        }
    }

    private fun playTracks(tracks: List<AudioTrack>) {
        val exoPlayer = player ?: return
        exoPlayer.setMediaItems(MediaItemFactory.createAll(tracks), 0, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun scheduleSleepTimer(endAtMillis: Long) {
        cancelSleepTimer()
        val delayMillis = (endAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val runnable = Runnable {
            player?.pause()
            sleepTimerRunnable = null
            serviceScope.launch { settingsRepository.setSleepTimerEndAt(null) }
        }
        sleepTimerRunnable = runnable
        handler.postDelayed(runnable, delayMillis)
    }

    private fun cancelSleepTimer() {
        sleepTimerRunnable?.let(handler::removeCallbacks)
        sleepTimerRunnable = null
    }

    private fun promoteToForegroundForPreparation() {
        val notification = NotificationCompat.Builder(this, PREPARATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("音频播放器")
            .setContentText("正在准备定时播放")
            .setOngoing(true)
            .build()
        startForeground(PREPARATION_NOTIFICATION_ID, notification)
    }

    private fun showTimerError(message: String) {
        val notification = NotificationCompat.Builder(this, ERROR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("定时播放")
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(ERROR_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                ERROR_CHANNEL_ID,
                "定时任务通知",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                PREPARATION_CHANNEL_ID,
                "播放准备",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    companion object {
        const val ACTION_START_TIMER = "com.example.audioplayer.action.START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.audioplayer.action.STOP_TIMER"
        const val ACTION_SET_SLEEP_TIMER = "com.example.audioplayer.action.SET_SLEEP_TIMER"
        const val ACTION_CANCEL_SLEEP_TIMER = "com.example.audioplayer.action.CANCEL_SLEEP_TIMER"
        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_SLEEP_TIMER_END_AT = "sleep_timer_end_at"

        private const val ERROR_CHANNEL_ID = "timer_errors"
        private const val PREPARATION_CHANNEL_ID = "playback_preparation"
        private const val PREPARATION_NOTIFICATION_ID = 1002
        private const val ERROR_NOTIFICATION_ID = 1001
    }
}
