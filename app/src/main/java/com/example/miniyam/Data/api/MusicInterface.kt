package com.example.miniyam.Data.api

import com.example.miniyam.Data.model.Music.LikeResponse
import com.example.miniyam.Data.model.Music.RequestSearchMusic
import com.example.miniyam.Domain.Track
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface MusicInterface{
    @GET("getTracks")
    suspend fun getTracks(
        @Header("Authorization") authHeader: String
    ): List<Track>

    @POST("searchTracks")
    suspend fun searchTracks(@Body body: RequestSearchMusic): List<Track>

    @POST("likeTrack/{id}")
    suspend fun likeTrack(
        @Path("id") trackId: Long,
        @Header("Authorization") token: String
    ): LikeResponse

    @GET("likedTracks")
    suspend fun getLikedTracks(
        @Header("Authorization") authHeader: String
    ): List<Track>
}