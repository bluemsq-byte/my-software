package com.example.audioplayer.core.network.webdav

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class WebDavClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun list_sendsBasicAuthenticationAndParsesEntries() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(207)
                .setBody(
                    """
                    <d:multistatus xmlns:d="DAV:">
                      <d:response>
                        <d:href>/dav/music/song.mp3</d:href>
                        <d:propstat>
                          <d:prop><d:resourcetype/><d:getcontentlength>42</d:getcontentlength></d:prop>
                          <d:status>HTTP/1.1 200 OK</d:status>
                        </d:propstat>
                      </d:response>
                    </d:multistatus>
                    """.trimIndent(),
                ),
        )

        val config = WebDavConnectionConfig(
            id = "nas",
            baseUrl = server.url("/dav/music/").toString(),
            username = "user",
            password = "password",
        )

        val entries = WebDavClient(OkHttpClient()).list(config)

        val request = server.takeRequest()
        assertThat(request.method).isEqualTo("PROPFIND")
        assertThat(request.getHeader("Authorization")).isEqualTo(Credentials.basic("user", "password"))
        assertThat(request.getHeader("Depth")).isEqualTo("1")
        assertThat(entries.single().name).isEqualTo("song.mp3")
        assertThat(entries.single().path).isEqualTo("/song.mp3")
    }

    @Test(expected = com.example.audioplayer.core.network.RemoteException.AuthenticationFailed::class)
    fun testConnection_mapsUnauthorizedResponse() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val config = WebDavConnectionConfig(
            id = "nas",
            baseUrl = server.url("/dav/").toString(),
            username = "bad",
            password = "bad",
        )

        WebDavClient(OkHttpClient()).testConnection(config)
    }
}