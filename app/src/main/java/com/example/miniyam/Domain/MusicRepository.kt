package com.example.miniyam.Domain

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import com.example.miniyam.Data.api.MusicService
import com.example.miniyam.Data.model.Music.LikeResponse
import com.example.miniyam.Data.model.Music.RequestSearchMusic
import com.example.miniyam.Domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMusic(private val context: Context) : MusicRepository {
    @OptIn(UnstableApi::class)
    override suspend fun getTracks(token:String): List<Track> {
        val trackList = mutableListOf<Track>()
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->

                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Title"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val duration = cursor.getInt(durationCol)
                    val path = cursor.getString(dataCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val artUri = Uri.withAppendedPath(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId.toString()
                    ).toString()
                    if (duration > 0) {
                        trackList.add(Track(id, title, artist, duration, artUri, path,true))
                    }
                }
            }
        }
        return trackList
    }

    override suspend fun searchTracks(query: String): List<Track> {
        return listOf()
    }

    override suspend fun likeTrack(trackId: Long, token: String): LikeResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getLikesTracks(token: String): List<Track> {
        TODO("Not yet implemented")
    }
}

class RemoteMusic(): MusicRepository {
    override suspend fun getTracks(token: String): List<Track> = withContext(Dispatchers.IO) {
        MusicService.api.getTracks("Bearer $token")
    }

    override suspend fun searchTracks(query: String): List<Track> = withContext(Dispatchers.IO) {
        MusicService.api.searchTracks(RequestSearchMusic(searchQuery=query))
    }

    override suspend fun likeTrack(trackId: Long, token:String): LikeResponse = withContext(Dispatchers.IO) {
       MusicService.api.likeTrack(
           trackId,
           "Bearer $token"
       )
    }

    override suspend fun getLikesTracks(token: String): List<Track> = withContext(Dispatchers.IO) {
        MusicService.api.getLikedTracks("Bearer $token")
    }
}
