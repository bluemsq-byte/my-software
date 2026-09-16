package com.example.audioplayer.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE connections ADD COLUMN selectedShare TEXT")
        database.execSQL("UPDATE connections SET selectedShare = share")

        database.execSQL(
            "ALTER TABLE timers ADD COLUMN selectionType TEXT NOT NULL DEFAULT 'FOLDER'",
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS timer_files (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timerId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                path TEXT NOT NULL,
                FOREIGN KEY(timerId) REFERENCES timers(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_timer_files_timerId ON timer_files(timerId)",
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recent_plays (
                mediaId TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                artist TEXT,
                album TEXT,
                durationMillis INTEGER NOT NULL,
                uri TEXT NOT NULL,
                artworkUri TEXT,
                sourceType TEXT NOT NULL,
                connectionId TEXT,
                remotePath TEXT,
                lastPlayedEpochMillis INTEGER NOT NULL,
                playCount INTEGER NOT NULL
            )
            """.trimIndent(),
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                createdAtEpochMillis INTEGER NOT NULL,
                updatedAtEpochMillis INTEGER NOT NULL
            )
            """.trimIndent(),
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS playlist_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                playlistId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                mediaId TEXT NOT NULL,
                title TEXT NOT NULL,
                artist TEXT,
                album TEXT,
                durationMillis INTEGER NOT NULL,
                uri TEXT NOT NULL,
                artworkUri TEXT,
                sourceType TEXT NOT NULL,
                connectionId TEXT,
                remotePath TEXT,
                FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_playlist_items_playlistId ON playlist_items(playlistId)",
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS playback_session (
                id INTEGER PRIMARY KEY NOT NULL,
                currentIndex INTEGER NOT NULL,
                positionMillis INTEGER NOT NULL,
                playbackMode TEXT NOT NULL,
                updatedAtEpochMillis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS playback_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                position INTEGER NOT NULL,
                mediaId TEXT NOT NULL,
                title TEXT NOT NULL,
                artist TEXT,
                album TEXT,
                durationMillis INTEGER NOT NULL,
                uri TEXT NOT NULL,
                artworkUri TEXT,
                sourceType TEXT NOT NULL,
                connectionId TEXT,
                remotePath TEXT
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_playback_queue_position ON playback_queue(position)",
        )
    }
}
