package com.example.miniyam.Data.api

import com.example.miniyam.BASEURL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MusicService {
    val api: MusicInterface by lazy{
        Retrofit.Builder()
            .baseUrl(BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicInterface::class.java)
    }
}