package com.example.miniyam.Domain.managers

import android.content.SharedPreferences
import com.example.miniyam.Domain.Track
import com.example.miniyam.Domain.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikesManager @Inject constructor(
    private val remoteMusic:MusicRepository,
    private val sharedPreferences: SharedPreferences,
) {

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likesTracks:StateFlow<List<Track>> =_likedTracks

    fun getLikedTrack() {
       CoroutineScope(Dispatchers.IO).launch {
           val token = sharedPreferences.getString("token", "") ?: ""
           if (token.isNotEmpty()) {
               val tracks = remoteMusic.getLikesTracks(token)
               _likedTracks.update{tracks}
           }
       }
    }

    fun isLikes(track: Track):Boolean{
        return _likedTracks.value.contains(track)
    }
    fun likeTrack(track: Track) {
        _likedTracks.update { current ->
            if (current.any { it.id == track.id }) {
                current.filterNot { it.id == track.id }
            } else {
                current + track
            }
        }
    }

    init {
        getLikedTrack()
    }

}