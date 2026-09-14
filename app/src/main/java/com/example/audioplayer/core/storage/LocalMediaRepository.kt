package com.example.audioplayer.core.storage

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class LocalMediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun scan(): List<AudioTrack> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.IS_MUSIC,
        )
        val tracks = mutableListOf<AudioTrack>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                tracks += cursor.toAudioTrack()
            }
        }

        tracks
    }

    private fun Cursor.toAudioTrack(): AudioTrack {
        val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)).orEmpty()
        val artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
            ?.takeUnless { it == "<unknown>" }
        val album = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
        val albumId = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
        val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
        val dataPath = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)).orEmpty()
        val folder = dataPath.substringBeforeLast('/', "/")

        return AudioTrack(
            id = "local:$id",
            title = title.ifBlank { dataPath.substringAfterLast('/') },
            artist = artist,
            album = album,
            durationMillis = duration,
            uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id,
            ).toString(),
            artworkUri = if (albumId > 0) {
                ContentUris.withAppendedId(ALBUM_ART_URI, albumId).toString()
            } else {
                null
            },
            sourceType = AudioSourceType.LOCAL,
            remotePath = folder,
        )
    }

    private companion object {
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}