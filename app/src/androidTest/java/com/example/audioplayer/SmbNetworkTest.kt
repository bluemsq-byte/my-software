package com.example.audioplayer

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.network.RemoteException
import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.network.smb.SmbShareEnumerator
import com.example.audioplayer.core.network.smb.SmbConnectionConfig
import com.example.audioplayer.core.network.webdav.WebDavClient
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.RemoteFileRepository
import com.example.audioplayer.core.security.AndroidCredentialStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmbNetworkTest {
    @Test
    fun repository_movesSmbNetworkWorkOffMainThread() = runBlocking {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val credentialStore = AndroidCredentialStore(context)
        val connectionId = "main-thread-test"
        try {
            credentialStore.save(connectionId, "password")
            database.connectionDao().upsert(
                ConnectionEntity(
                    id = connectionId,
                    name = "unreachable",
                    protocol = ConnectionProtocol.SMB,
                    host = "127.0.0.1",
                    port = 1,
                    username = "user",
                    share = null,
                    selectedShare = null,
                    basePath = "/",
                    domain = null,
                    useHttps = true,
                ),
            )
            val connections = ConnectionRepository(database.connectionDao(), credentialStore)
            val repository = RemoteFileRepository(
                connectionRepository = connections,
                webDavClient = WebDavClient(),
                smbClient = SmbClient(),
                smbShareEnumerator = SmbShareEnumerator(),
            )
            val connection = requireNotNull(connections.get(connectionId))
            val error = withContext(Dispatchers.Main) {
                runCatching { repository.test(connection) }.exceptionOrNull()
            }
            org.junit.Assert.assertTrue(error is RemoteException)
        } finally {
            credentialStore.remove(connectionId)
            database.close()
        }
    }

    @Test
    fun unreachableSmbServer_mapsToUserFacingNetworkError() = runBlocking {
        val client = SmbClient()
        val config = SmbConnectionConfig(
            id = "unreachable",
            host = "127.0.0.1",
            port = 1,
            username = "user",
            password = "password",
            share = "music",
        )

        val error = runCatching { client.testConnection(config) }.exceptionOrNull()
        check(error is RemoteException.Unreachable) {
            "Expected RemoteException.Unreachable, got $error"
        }
    }
}