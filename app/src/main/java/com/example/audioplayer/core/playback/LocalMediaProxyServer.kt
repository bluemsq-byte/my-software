@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.playback

import android.content.Context
import android.net.Uri
import androidx.media3.datasource.DataSpec
import com.example.audioplayer.core.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedOutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMediaProxyServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSourceFactory: AppDataSourceFactory,
) {
    private val executor = Executors.newCachedThreadPool()
    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var session: Session? = null

    fun start(track: AudioTrack): String {
        stop()
        val server = ServerSocket(0)
        serverSocket = server
        val token = UUID.randomUUID().toString()
        session = Session(track, token)
        running.set(true)
        executor.execute {
            while (running.get()) {
                try {
                    val socket = server.accept()
                    executor.execute { handle(socket) }
                } catch (_: SocketException) {
                    break
                } catch (_: Exception) {
                    if (!running.get()) break
                }
            }
        }
        val host = findLocalIpv4() ?: error("No local Wi-Fi address")
        return "http://$host:${server.localPort}/media/$token"
    }

    fun stop() {
        running.set(false)
        try {
            serverSocket?.close()
        } catch (_: Exception) {
            // Best effort.
        }
        serverSocket = null
        session = null
    }

    private fun handle(socket: Socket) {
        socket.use { client ->
            val input = client.getInputStream().bufferedReader()
            val requestLine = input.readLine().orEmpty()
            val parts = requestLine.split(' ')
            if (parts.size < 2) return
            val method = parts[0]
            val requestPath = parts[1]
            var rangeHeader: String? = null
            while (true) {
                val line = input.readLine() ?: break
                if (line.isBlank()) break
                if (line.startsWith("Range:", ignoreCase = true)) rangeHeader = line.substringAfter(':').trim()
            }

            val active = session ?: return
            if (requestPath != "/media/${active.token}") {
                writeStatus(client, 403, "Forbidden")
                return
            }
            val spec = parseRange(rangeHeader).let { range ->
                DataSpec(
                    Uri.parse(active.track.uri),
                    range?.first ?: 0L,
                    range?.let { it.second - it.first + 1 } ?: -1L,
                )
            }
            val source = dataSourceFactory.createDataSource()
            try {
                val length = source.open(spec)
                val output = BufferedOutputStream(client.getOutputStream())
                val status = if (rangeHeader == null) "200 OK" else "206 Partial Content"
                output.write("HTTP/1.1 $status\r\n".toByteArray())
                output.write("Content-Type: ${active.contentType}\r\n".toByteArray())
                output.write("Accept-Ranges: bytes\r\n".toByteArray())
                output.write("Access-Control-Allow-Origin: *\r\n".toByteArray())
                if (rangeHeader != null) {
                    val start = rangeHeader.substringAfter("bytes=").substringBefore('-').toLongOrNull() ?: 0L
                    output.write("Content-Range: bytes $start-${start + length - 1}/*\r\n".toByteArray())
                }
                output.write("Content-Length: $length\r\n".toByteArray())
                output.write("Connection: close\r\n\r\n".toByteArray())
                if (method != "HEAD") {
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = source.read(buffer, 0, buffer.size)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                    }
                }
                output.flush()
            } finally {
                source.close()
            }
        }
    }

    private fun parseRange(header: String?): Pair<Long, Long>? {
        if (header == null || !header.startsWith("bytes=")) return null
        val value = header.removePrefix("bytes=").substringBefore(',')
        val start = value.substringBefore('-').toLongOrNull() ?: return null
        val end = value.substringAfter('-').toLongOrNull() ?: Long.MAX_VALUE
        return start to end
    }

    private fun writeStatus(socket: Socket, code: Int, text: String) {
        socket.getOutputStream().write("HTTP/1.1 $code $text\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
    }

    private fun findLocalIpv4(): String? {
        return NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && it.isSiteLocalAddress }
            ?.hostAddress
    }

    private data class Session(
        val track: AudioTrack,
        val token: String,
        val contentType: String = track.contentType(),
    )

}

private fun AudioTrack.contentType(): String {
    val extension = uri.substringBefore('?').substringAfterLast('.', "").lowercase()
    return when (extension) {
        "wav" -> "audio/wav"
        "flac" -> "audio/flac"
        "m4a" -> "audio/mp4"
        "aac" -> "audio/aac"
        "ogg", "opus" -> "audio/ogg"
        else -> "audio/mpeg"
    }
}
