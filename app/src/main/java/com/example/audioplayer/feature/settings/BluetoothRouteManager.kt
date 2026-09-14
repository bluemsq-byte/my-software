package com.example.audioplayer.feature.settings

import android.content.Context
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BluetoothAudioDevice(
    val id: String,
    val name: String,
    val isSelected: Boolean,
)

@Singleton
class BluetoothRouteManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val router = MediaRouter.getInstance(context)
    private val _devices = MutableStateFlow<List<BluetoothAudioDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothAudioDevice>> = _devices.asStateFlow()

    private val callback = object : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()
        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()
        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) = refresh()
        override fun onRouteSelected(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            reason: Int,
        ) = refresh()
    }

    init {
        router.addCallback(
            MediaRouteSelector.EMPTY,
            callback,
            MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY,
        )
        refresh()
    }

    fun refresh() {
        _devices.value = router.routes
            .filter {
                it.deviceType == MediaRouter.RouteInfo.DEVICE_TYPE_BLUETOOTH_A2DP ||
                    it.deviceType == MediaRouter.RouteInfo.DEVICE_TYPE_BLE_HEADSET
            }
            .map { route ->
                BluetoothAudioDevice(
                    id = route.id,
                    name = route.name,
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
}