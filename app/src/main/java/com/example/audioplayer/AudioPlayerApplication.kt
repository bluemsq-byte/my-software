package com.example.audioplayer

import android.app.Application
import com.example.audioplayer.core.scheduler.TimerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class AudioPlayerApplication : Application() {
    @Inject lateinit var timerScheduler: TimerScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            timerScheduler.rescheduleAll()
        }
    }
}
