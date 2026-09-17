package com.example.audioplayer

import android.app.AlarmManager
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSourceType
import com.example.audioplayer.core.model.TimerTask
import com.example.audioplayer.core.repository.TimerRepository
import com.example.audioplayer.core.scheduler.TimerScheduler
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerSchedulerTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exactAlarm_schedulesAndCancelsOnDevice() = runBlocking {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            grantExactAlarmPermission()
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = TimerRepository(database.timerDao(), database.timerFileDao())
        val scheduler = TimerScheduler(context, repository)
        assertTrue(scheduler.canScheduleExactAlarms())

        val future = LocalDateTime.now().plusMinutes(2)
        val task = TimerTask(
            id = 77L,
            name = "instrumentation",
            action = TimerAction.START,
            hour = future.hour,
            minute = future.minute,
            repeatDays = emptySet(),
            enabled = true,
            sourceType = TimerSourceType.LOCAL_FOLDER,
            sourcePath = "/Music",
        )
        repository.save(task)
        scheduler.schedule(task)
        scheduler.cancel(task.id)
    }

    @Test
    fun enabledTimers_canBeRescheduledForApplicationRestart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = TimerRepository(database.timerDao(), database.timerFileDao())
        val scheduler = TimerScheduler(context, repository)
        val future = LocalDateTime.now().plusMinutes(3)
        val task = TimerTask(
            id = 78L,
            name = "restart",
            action = TimerAction.START,
            hour = future.hour,
            minute = future.minute,
            repeatDays = emptySet(),
            enabled = true,
            sourceType = TimerSourceType.LOCAL_FOLDER,
            sourcePath = "/Music",
        )
        repository.save(task)

        scheduler.rescheduleAll()
        scheduler.cancel(task.id)
    }

    private fun grantExactAlarmPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        val descriptor = instrumentation.uiAutomation.executeShellCommand(
            "appops set $packageName SCHEDULE_EXACT_ALARM allow",
        )
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }
}
