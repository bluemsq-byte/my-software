package com.example.audioplayer.core.network.smb

import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemoteException
import com.example.audioplayer.core.network.RemotePath
import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SMBRuntimeException
import java.io.Closeable
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

class SmbClient(
    private val sessionFactory: (SmbConnectionConfig) -> SmbShareSession = DefaultSmbShareSession::connect,
) : Closeable {
    private val sessions = ConcurrentHashMap<String, SmbShareSession>()

    fun testConnection(config: SmbConnectionConfig) {
        runSmbOperation {
            session(config).list("\\")
        }
    }

    fun list(
        config: SmbConnectionConfig,
        path: String = "/",
    ): List<RemoteEntry> = runSmbOperation {
        session(config).list(RemotePath.toSmb(path)).map { item ->
            RemoteEntry(
                name = item.name,
                path = RemotePath.fromSmb(item.path),
                isDirectory = item.isDirectory,
                size = item.size,
                lastModifiedEpochMillis = item.lastModifiedEpochMillis,
            )
        }.sortedWith(
            compareByDescending<RemoteEntry> { it.isDirectory }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
        )
    }

    fun openFile(
        config: SmbConnectionConfig,
        path: String,
    ): SmbReadableFile = runSmbOperation {
        session(config).openFile(RemotePath.toSmb(path))
    }

    fun closeConnection(connectionId: String) {
        sessions.remove(connectionId)?.close()
    }

    override fun close() {
        sessions.values.forEach { session ->
            try {
                session.close()
            } catch (_: Exception) {
                // Continue closing other sessions.
            }
        }
        sessions.clear()
    }

    private fun session(config: SmbConnectionConfig): SmbShareSession {
        return sessions.computeIfAbsent(config.id) {
            try {
                sessionFactory(config)
            } catch (exception: Exception) {
                throw mapException(exception)
            }
        }
    }

    private fun <T> runSmbOperation(block: () -> T): T {
        return try {
            block()
        } catch (exception: RemoteException) {
            throw exception
        } catch (exception: Exception) {
            throw mapException(exception)
        }
    }

    private fun mapException(exception: Exception): RemoteException {
        return when (exception) {
            is SMBApiException -> when (exception.status) {
                NtStatus.STATUS_LOGON_FAILURE,
                NtStatus.STATUS_ACCESS_DENIED,
                NtStatus.STATUS_LOGON_TYPE_NOT_GRANTED,
                -> RemoteException.AuthenticationFailed()
                NtStatus.STATUS_OBJECT_NAME_NOT_FOUND,
                NtStatus.STATUS_OBJECT_PATH_NOT_FOUND,
                NtStatus.STATUS_BAD_NETWORK_NAME,
                NtStatus.STATUS_NETWORK_NAME_DELETED,
                -> RemoteException.PathNotFound()
                else -> RemoteException.ProtocolError("SMB 操作失败：${exception.message.orEmpty()}")
            }

            is UnknownHostException -> RemoteException.Unreachable("找不到 NAS，请检查地址和网络", exception)
            is IOException -> RemoteException.Unreachable("无法连接 NAS", exception)
            is SMBRuntimeException -> RemoteException.ProtocolError("SMB 协议错误：${exception.message.orEmpty()}")
            else -> RemoteException.ProtocolError(exception.message ?: "SMB 连接失败")
        }
    }
}