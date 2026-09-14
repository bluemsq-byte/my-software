package com.example.audioplayer.core.repository

import com.example.audioplayer.core.database.TimerDao
import com.example.audioplayer.core.database.TimerEntity
import com.example.audioplayer.core.model.TimerTask
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TimerRepository @Inject constructor(
    private val timerDao: TimerDao,
) {
    fun observeAll(): Flow<List<TimerTask>> = timerDao.observeAll().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun get(id: Long): TimerTask? = timerDao.getById(id)?.toModel()

    suspend fun getEnabled(): List<TimerTask> = timerDao.getEnabled().map { it.toModel() }

    suspend fun save(task: TimerTask): Long {
        return timerDao.upsert(task.toEntity())
    }

    suspend fun delete(task: TimerTask) {
        timerDao.delete(task.toEntity())
    }

    private fun TimerEntity.toModel() = TimerTask(
        id = id,
        name = name,
        action = action,
        hour = hour,
        minute = minute,
        repeatDays = DayOfWeek.entries.filterTo(mutableSetOf()) { day ->
            repeatDaysMask and (1 shl day.value) != 0
        },
        enabled = enabled,
        sourceType = sourceType,
        connectionId = connectionId,
        sourcePath = sourcePath,
        createdAtEpochMillis = createdAtEpochMillis,
        lastRunEpochMillis = lastRunEpochMillis,
    )

    private fun TimerTask.toEntity() = TimerEntity(
        id = id,
        name = name,
        action = action,
        hour = hour,
        minute = minute,
        repeatDaysMask = repeatDays.fold(0) { mask, day -> mask or (1 shl day.value) },
        enabled = enabled,
        sourceType = sourceType,
        connectionId = connectionId,
        sourcePath = sourcePath,
        createdAtEpochMillis = createdAtEpochMillis,
        lastRunEpochMillis = lastRunEpochMillis,
    )
}