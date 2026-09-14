package com.example.audioplayer

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.audioplayer.core.network.RemoteException
import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.network.smb.SmbConnectionConfig
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmbNetworkTest {
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