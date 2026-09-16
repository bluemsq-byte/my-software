package com.example.audioplayer

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.database.MIGRATION_1_2
import com.example.audioplayer.core.database.MIGRATION_2_3
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2_preservesConnectionsAndTimers() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                """
                INSERT INTO connections
                (id, name, protocol, host, port, username, share, basePath, domain, useHttps)
                VALUES ('nas', '旧 NAS', 'SMB', '192.168.1.2', 445, 'user', 'music', '/', NULL, 1)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO timers
                (id, name, action, hour, minute, repeatDaysMask, enabled, sourceType,
                 connectionId, sourcePath, createdAtEpochMillis, lastRunEpochMillis)
                VALUES (1, '早晨', 'START', 7, 30, 0, 1, 'SMB_FOLDER',
                        'nas', '/music', 1000, NULL)
                """.trimIndent(),
            )
            close()
        }

        val database = helper.runMigrationsAndValidate(
            TEST_DATABASE,
            2,
            true,
            MIGRATION_1_2,
        )

        database.query("SELECT selectedShare FROM connections WHERE id = 'nas'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("music")
        }
        database.query("SELECT selectionType FROM timers WHERE id = 1").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("FOLDER")
        }
        database.query("SELECT COUNT(*) FROM timer_files").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getInt(0)).isEqualTo(0)
        }
        database.close()
    }

    @Test
    fun migrate2To3_createsPlaybackSessionTables() {
        helper.createDatabase(TEST_DATABASE, 2).close()

        val database = helper.runMigrationsAndValidate(
            TEST_DATABASE,
            3,
            true,
            MIGRATION_2_3,
        )

        database.query("SELECT COUNT(*) FROM playback_session").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getInt(0)).isEqualTo(0)
        }
        database.query("SELECT COUNT(*) FROM playback_queue").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getInt(0)).isEqualTo(0)
        }
        database.close()
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
