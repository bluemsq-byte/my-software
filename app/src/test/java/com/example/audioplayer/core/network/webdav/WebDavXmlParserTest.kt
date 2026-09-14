package com.example.audioplayer.core.network.webdav

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WebDavXmlParserTest {
    @Test
    fun parse_returnsDirectoriesFirstAndFiltersCurrentDirectory() {
        val entries = WebDavXmlParser.parse(
            xml = MULTISTATUS,
            baseUrl = "http://nas.local:5005/music/",
            currentPath = "/",
        )

        assertThat(entries.map { it.name }).containsExactly("album", "song.mp3").inOrder()
        assertThat(entries.first().isDirectory).isTrue()
        assertThat(entries.last().size).isEqualTo(12345L)
    }

    @Test
    fun parse_usesRelativePathBelowConfiguredBasePath() {
        val entries = WebDavXmlParser.parse(
            xml = MULTISTATUS.replace("/music/", "/dav/music/"),
            baseUrl = "http://nas.local:5005/dav/music/",
            currentPath = "/",
        )

        assertThat(entries.map { it.path }).containsExactly("/album", "/song.mp3").inOrder()
    }

    private companion object {
        val MULTISTATUS = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/music/</d:href>
                <d:propstat>
                  <d:prop><d:resourcetype><d:collection/></d:resourcetype></d:prop>
                  <d:status>HTTP/1.1 200 OK</d:status>
                </d:propstat>
              </d:response>
              <d:response>
                <d:href>/music/song.mp3</d:href>
                <d:propstat>
                  <d:prop>
                    <d:resourcetype/>
                    <d:getcontentlength>12345</d:getcontentlength>
                    <d:getlastmodified>Sun, 14 Sep 2026 08:00:00 GMT</d:getlastmodified>
                  </d:prop>
                  <d:status>HTTP/1.1 200 OK</d:status>
                </d:propstat>
              </d:response>
              <d:response>
                <d:href>/music/album/</d:href>
                <d:propstat>
                  <d:prop><d:resourcetype><d:collection/></d:resourcetype></d:prop>
                  <d:status>HTTP/1.1 200 OK</d:status>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()
    }
}