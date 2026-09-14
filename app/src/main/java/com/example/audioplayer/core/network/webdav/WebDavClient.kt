package com.example.audioplayer.core.network.webdav

import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemoteException
import com.example.audioplayer.core.network.RemotePath
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WebDavClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    suspend fun testConnection(config: WebDavConnectionConfig) {
        propfind(config, "/", depth = 0)
    }

    suspend fun list(
        config: WebDavConnectionConfig,
        path: String = "/",
    ): List<RemoteEntry> = withContext(Dispatchers.IO) {
        propfind(config, RemotePath.normalize(path), depth = 1)
    }

    private fun propfind(
        config: WebDavConnectionConfig,
        path: String,
        depth: Int,
    ): List<RemoteEntry> {
        val request = Request.Builder()
            .url(resolveUrl(config.baseUrl, path))
            .header("Authorization", Credentials.basic(config.username, config.password))
            .header("Depth", depth.toString())
            .header("Content-Type", "application/xml; charset=utf-8")
            .method("PROPFIND", PROPFIND_BODY.toRequestBody("application/xml; charset=utf-8".toMediaType()))
            .build()

        val response = try {
            httpClient.newCall(request).execute()
        } catch (exception: UnknownHostException) {
            throw RemoteException.Unreachable("找不到服务器，请检查地址和网络", exception)
        } catch (exception: SocketTimeoutException) {
            throw RemoteException.Unreachable("连接服务器超时", exception)
        } catch (exception: SSLHandshakeException) {
            throw RemoteException.CertificateRejected()
        } catch (exception: IOException) {
            throw RemoteException.Unreachable("无法连接服务器", exception)
        }

        response.use {
            when (it.code) {
                200, 207 -> Unit
                401, 403 -> throw RemoteException.AuthenticationFailed()
                404 -> throw RemoteException.PathNotFound()
                else -> throw RemoteException.ProtocolError("WebDAV 请求失败，状态码 ${it.code}")
            }

            val body = it.body?.string().orEmpty()
            if (depth == 0) return emptyList()
            return WebDavXmlParser.parse(body, config.baseUrl, path)
        }
    }

    internal fun resolveUrl(baseUrl: String, path: String): HttpUrl {
        val base = try {
            baseUrl.toHttpUrl()
        } catch (exception: IllegalArgumentException) {
            throw RemoteException.ProtocolError("WebDAV 地址格式不正确")
        }
        val builder = base.newBuilder()
            .encodedPath("/")
            .query(null)
            .fragment(null)

        base.pathSegments
            .filter { it.isNotBlank() }
            .forEach(builder::addPathSegment)

        RemotePath.normalize(path)
            .split('/')
            .filter { it.isNotBlank() }
            .forEach(builder::addPathSegment)

        return builder.build()
    }

    private companion object {
        val PROPFIND_BODY = """
            <?xml version="1.0" encoding="utf-8" ?>
            <d:propfind xmlns:d="DAV:">
              <d:prop>
                <d:resourcetype />
                <d:getcontentlength />
                <d:getlastmodified />
              </d:prop>
            </d:propfind>
        """.trimIndent()
    }
}