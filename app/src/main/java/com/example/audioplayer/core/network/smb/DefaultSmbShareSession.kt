package com.example.audioplayer.core.network.smb

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import java.util.EnumSet

class DefaultSmbShareSession private constructor(
    private val client: SMBClient,
    private val connection: Connection,
    private val session: Session,
    private val share: DiskShare,
) : SmbShareSession {
    override fun list(path: String): List<SmbDirectoryItem> {
        return share.list(RemoteSmbPath.fromRemote(path)).mapNotNull { item ->
            val name = item.fileName
            if (name == "." || name == "..") return@mapNotNull null
            val childPath = RemoteSmbPath.join(path, name)
            SmbDirectoryItem(
                name = name,
                path = childPath,
                isDirectory = item.fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value != 0L,
                size = item.endOfFile,
                lastModifiedEpochMillis = item.lastWriteTime?.toEpochMillis(),
            )
        }.sortedWith(
            compareByDescending<SmbDirectoryItem> { it.isDirectory }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
        )
    }

    override fun openFile(path: String): SmbReadableFile {
        val file = share.openFile(
            RemoteSmbPath.fromRemote(path),
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null,
        )
        return SmbFileHandle(file)
    }

    override fun close() {
        closeQuietly(share)
        closeQuietly(session)
        closeQuietly(connection)
        closeQuietly(client)
    }

    private class SmbFileHandle(
        private val file: File,
    ) : SmbReadableFile {
        override val length: Long
            get() = file.fileInformation.standardInformation.endOfFile

        override fun read(
            buffer: ByteArray,
            fileOffset: Long,
            bufferOffset: Int,
            length: Int,
        ): Int = file.read(buffer, fileOffset, bufferOffset, length)

        override fun close() {
            closeQuietly(file)
        }
    }

    companion object {
        fun connect(config: SmbConnectionConfig): DefaultSmbShareSession {
            val client = SMBClient()
            var connection: Connection? = null
            var session: Session? = null
            var share: DiskShare? = null

            try {
                connection = client.connect(config.host, config.port)
                session = connection.authenticate(
                    AuthenticationContext(
                        config.username,
                        config.password.toCharArray(),
                        config.domain,
                    ),
                )
                share = session.connectShare(config.share) as? DiskShare
                    ?: throw IllegalStateException("SMB 共享目录不是磁盘共享")
                return DefaultSmbShareSession(client, connection, session, share)
            } catch (exception: Exception) {
                share?.let(::closeQuietly)
                session?.let(::closeQuietly)
                connection?.let(::closeQuietly)
                closeQuietly(client)
                throw exception
            }
        }

        private fun closeQuietly(closeable: AutoCloseable?) {
            try {
                closeable?.close()
            } catch (_: Exception) {
                // Closing should not hide the original connection result.
            }
        }
    }
}

internal object RemoteSmbPath {
    fun fromRemote(path: String): String = path
        .replace('/', '\\')
        .trimStart('\\')

    fun join(base: String, child: String): String {
        val left = base.replace('/', '\\').trim('\\')
        val right = child.replace('/', '\\').trim('\\')
        return when {
            left.isBlank() -> "\\$right"
            else -> "\\$left\\$right"
        }
    }
}