package com.example.miniyam.Domain.repository

import com.example.miniyam.Data.model.Music.LikeResponse
import com.example.miniyam.Domain.Track

interface MusicRepository {
    suspend fun getTracks(token:String): List<Track>
    suspend fun searchTracks(query: String):List<Track>
    suspend fun likeTrack(trackId: Long,token:String):LikeResponse
    suspend fun getLikesTracks(token:String):List<Track>
}