package com.example.audioplayer.core.repository

import com.example.audioplayer.core.database.TimerDao
import com.example.audioplayer.core.database.TimerEntity
import com.example.audioplayer.core.database.TimerFileDao
import com.example.audioplayer.core.database.TimerFileEntity
import com.example.audioplayer.core.model.TimerTask
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TimerRepository @Inject constructor(
    private val timerDao: TimerDao,
    private val timerFileDao: TimerFileDao,
) {
    fun observeAll(): Flow<List<TimerTask>> = timerDao.observeAll().map { entities ->
        entities.map { entity ->
            entity.toModel(timerFileDao.getForTimer(entity.id).map(TimerFileEntity::path))
        }
    }

    suspend fun get(id: Long): TimerTask? {
        val entity = timerDao.getById(id) ?: return null
        return entity.toModel(timerFileDao.getForTimer(id).map(TimerFileEntity::path))
    }

    suspend fun getEnabled(): List<TimerTask> = timerDao.getEnabled().map { entity ->
        entity.toModel(timerFileDao.getForTimer(entity.id).map(TimerFileEntity::path))
    }

    suspend fun save(task: TimerTask): Long {
        val savedId = timerDao.upsert(task.toEntity())
        val finalId = if (task.id == 0L) savedId else task.id
        timerFileDao.deleteForTimer(finalId)
        if (task.selectedFiles.isNotEmpty()) {
            timerFileDao.insertAll(
                task.selectedFiles.mapIndexed { index, path ->
                    TimerFileEntity(
                        timerId = finalId,
                        position = index,
                        path = path,
                    )
                },
            )
        }
        return finalId
    }

    suspend fun delete(task: TimerTask) {
        timerDao.delete(task.toEntity())
    }

    private fun TimerEntity.toModel(selectedFiles: List<String>) = TimerTask(
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
        selectionType = selectionType,
        connectionId = connectionId,
        sourcePath = sourcePath,
        selectedFiles = selectedFiles,
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
        selectionType = selectionType,
        connectionId = connectionId,
        sourcePath = sourcePath,
        createdAtEpochMillis = createdAtEpochMillis,
        lastRunEpochMillis = lastRunEpochMillis,
    )
}