package com.example.miniyam.Interfaces
import com.example.miniyam.BASEURL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.example.miniyam.Track
import retrofit2.http.Body
import retrofit2.http.POST

data class RequestSearchMusic(
    val searchQuery: String
)

object MusicCatalogService {
    val api:MusicCatalogInterface by lazy{
        Retrofit.Builder()
            .baseUrl(BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicCatalogInterface::class.java)
    }
}

interface MusicCatalogInterface{
    @GET("getTracks")
    suspend fun getTracks() : List<Track>

    @POST("searchTracks")
    suspend fun searchTracks(@Body body: RequestSearchMusic): List<Track>
}












