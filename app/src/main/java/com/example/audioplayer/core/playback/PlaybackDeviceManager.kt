package com.example.audioplayer.core.playback

import android.content.Context
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlaybackDeviceType {
    BLUETOOTH,
    CAST,
    SYSTEM,
}

data class PlaybackDevice(
    val id: String,
    val name: String,
    val type: PlaybackDeviceType,
    val isSelected: Boolean,
)

@Singleton
class PlaybackDeviceManager @Inject constructor(
    @ApplicationContext context: Context,
    private val mediaProxyServer: LocalMediaProxyServer,
) {
    private val router = MediaRouter.getInstance(context)
    private val castContext = CastContext.getSharedInstance(context)
    private val castSelector = MediaRouteSelector.Builder()
        .addControlCategory(CastMediaControlIntent.categoryForCast(DEFAULT_RECEIVER_ID))
        .build()
    private val _devices = MutableStateFlow<List<PlaybackDevice>>(emptyList())
    val devices: StateFlow<List<PlaybackDevice>> = _devices.asStateFlow()
    private var pendingTrack: com.example.audioplayer.core.model.AudioTrack? = null

    private val callback = object : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()
        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()
        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()
        override fun onRouteSelected(router: MediaRouter, route: MediaRouter.RouteInfo, reason: Int) = refresh()
    }

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            if (pendingTrack != null) loadOnCastSession(session)
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            if (pendingTrack != null) loadOnCastSession(session)
        }

        override fun onSessionEnding(session: CastSession) = Unit

        override fun onSessionEnded(session: CastSession, error: Int) {
            mediaProxyServer.stop()
            pendingTrack = null
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) = Unit
        override fun onSessionStarting(session: CastSession) = Unit
        override fun onSessionResuming(session: CastSession, sessionId: String) = Unit
        override fun onSessionResumeFailed(session: CastSession, error: Int) = Unit
        override fun onSessionSuspended(session: CastSession, reason: Int) = Unit
    }

    init {
        router.addCallback(MediaRouteSelector.EMPTY, callback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        castContext.sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
        refresh()
    }

    fun refresh() {
        _devices.value = router.routes
            .filter { route ->
                route.deviceType == MediaRouter.RouteInfo.DEVICE_TYPE_BLUETOOTH_A2DP ||
                    route.deviceType == MediaRouter.RouteInfo.DEVICE_TYPE_BLE_HEADSET ||
                    route.matchesSelector(castSelector)
            }
            .map { route ->
                PlaybackDevice(
                    id = route.id,
                    name = route.name,
                    type = if (route.matchesSelector(castSelector)) {
                        PlaybackDeviceType.CAST
                    } else {
                        PlaybackDeviceType.BLUETOOTH
                    },
                    isSelected = route.isSelected,
                )
            }
    }

    fun select(deviceId: String): Boolean {
        val route = router.routes.firstOrNull { it.id == deviceId } ?: return false
        if (!route.isEnabled) return false
        route.select()
        refresh()
        return true
    }

    suspend fun playOnCast(track: com.example.audioplayer.core.model.AudioTrack): Boolean {
        pendingTrack = track
        val session = castContext.sessionManager.currentCastSession
        return if (session != null) {
            loadOnCastSession(session)
        } else {
            true
        }
    }

    private fun loadOnCastSession(session: CastSession): Boolean {
        val track = pendingTrack ?: return false
        val client = session.remoteMediaClient ?: return false
        val contentUrl = mediaProxyServer.start(track)
        val mediaInfo = MediaInfo.Builder(contentUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(track.mimeType())
            .build()
        client.load(MediaLoadRequestData.Builder().setMediaInfo(mediaInfo).build())
        return true
    }

    private fun com.example.audioplayer.core.model.AudioTrack.mimeType(): String {
        val extension = uri.substringBefore('?').substringAfterLast('.', "").lowercase()
        return when (extension) {
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg", "opus" -> "audio/ogg"
            else -> "audio/mpeg"
        }
    }

    private companion object {
        const val DEFAULT_RECEIVER_ID = "CC1AD845"
    }
}