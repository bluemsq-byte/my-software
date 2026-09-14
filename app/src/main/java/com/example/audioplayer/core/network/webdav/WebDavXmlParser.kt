package com.example.audioplayer.core.network.webdav

import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemoteException
import com.example.audioplayer.core.network.RemotePath
import java.io.StringReader
import java.net.URI
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

object WebDavXmlParser {
    fun parse(
        xml: String,
        baseUrl: String,
        currentPath: String,
    ): List<RemoteEntry> {
        if (xml.isBlank()) return emptyList()

        val document = try {
            DocumentBuilderFactory.newInstance()
                .apply {
                    isNamespaceAware = true
                    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                    setFeature("http://xml.org/sax/features/external-general-entities", false)
                    setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                    setExpandEntityReferences(false)
                }
                .newDocumentBuilder()
                .parse(InputSource(StringReader(xml)))
        } catch (exception: Exception) {
            throw RemoteException.ProtocolError("WebDAV 返回了无法解析的目录数据")
        }

        val normalizedCurrentPath = RemotePath.normalize(currentPath)
        val responses = document.getElementsByTagNameNS("*", "response")
        val entries = mutableListOf<RemoteEntry>()

        for (index in 0 until responses.length) {
            val response = responses.item(index) as? Element ?: continue
            val href = response.firstText("href") ?: continue
            val propElement = response.firstSuccessfulProp() ?: continue
            val path = relativePath(baseUrl, href)
            if (path == normalizedCurrentPath) continue

            val isDirectory = propElement
                .getElementsByTagNameNS("*", "collection")
                .length > 0

            val size = propElement.firstText("getcontentlength")?.toLongOrNull() ?: 0L
            val modified = propElement.firstText("getlastmodified").toEpochMillisOrNull()

            entries += RemoteEntry(
                name = RemotePath.name(path),
                path = path,
                isDirectory = isDirectory,
                size = size,
                lastModifiedEpochMillis = modified,
            )
        }

        return entries.sortedWith(
            compareByDescending<RemoteEntry> { it.isDirectory }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
        )
    }

    private fun Element.firstSuccessfulProp(): Element? {
        val propStats = getElementsByTagNameNS("*", "propstat")
        for (index in 0 until propStats.length) {
            val propStat = propStats.item(index) as? Element ?: continue
            val status = propStat.firstText("status").orEmpty()
            if (!status.contains(" 200 ") && !status.endsWith(" 200 OK")) continue
            val props = propStat.getElementsByTagNameNS("*", "prop")
            if (props.length > 0) return props.item(0) as? Element
        }
        return null
    }

    private fun Element.firstText(localName: String): String? {
        val elements = getElementsByTagNameNS("*", localName)
        if (elements.length == 0) return null
        return elements.item(0).textContent?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun relativePath(baseUrl: String, href: String): String {
        return try {
            val baseUri = URI(baseUrl)
            val targetUri = baseUri.resolve(href)
            val basePath = baseUri.path.orEmpty().trimEnd('/')
            val targetPath = targetUri.path.orEmpty()
            val relative = when {
                targetPath == basePath -> "/"
                targetPath.startsWith("$basePath/") -> targetPath.removePrefix(basePath)
                else -> href
            }
            RemotePath.normalize(relative)
        } catch (_: Exception) {
            RemotePath.normalize(href)
        }
    }

    private fun String?.toEpochMillisOrNull(): Long? {
        if (this == null) return null
        return try {
            ZonedDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            try {
                Instant.parse(this).toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }
    }
}