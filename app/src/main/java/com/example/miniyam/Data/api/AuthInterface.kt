package com.example.miniyam.Data.api

import com.example.miniyam.Data.model.Auth.LoginRequest
import com.example.miniyam.Data.model.Auth.RegisterRequest
import com.example.miniyam.Data.model.Auth.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthInterface {
    @POST("register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse
    @POST("login")
    suspend fun login(@Body body: LoginRequest): RegisterResponse
}