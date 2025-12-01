package com.example.miniyam.Data.api

import com.example.miniyam.BASEURL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AuthService {
    val api: AuthInterface by lazy{
        Retrofit.Builder()
            .baseUrl(BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthInterface::class.java)
    }
}