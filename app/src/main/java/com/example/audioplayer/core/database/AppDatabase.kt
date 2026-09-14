package com.example.audioplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ConnectionEntity::class,
        TimerEntity::class,
        TimerFileEntity::class,
        RecentPlayEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
    abstract fun timerDao(): TimerDao
    abstract fun timerFileDao(): TimerFileDao
    abstract fun recentPlayDao(): RecentPlayDao
    abstract fun playlistDao(): PlaylistDao
}