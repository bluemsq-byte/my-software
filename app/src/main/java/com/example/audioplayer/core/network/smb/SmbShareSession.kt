package com.example.audioplayer.core.network.smb

import java.io.Closeable

data class SmbDirectoryItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModifiedEpochMillis: Long?,
)

interface SmbReadableFile : Closeable {
    val length: Long

    fun read(
        buffer: ByteArray,
        fileOffset: Long,
        bufferOffset: Int,
        length: Int,
    ): Int
}

interface SmbShareSession : Closeable {
    fun list(path: String): List<SmbDirectoryItem>

    fun openFile(path: String): SmbReadableFile
}