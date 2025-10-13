package com.example.miniyam.Interfaces

import com.example.miniyam.BASEURL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterResponse(
    val message:String
)
data class RegisterRequest(
    val name:String,
    val email:String,
    val password:String
)
data class LoginRequest(
    val email: String,
    val password: String
)

object AuthService {
    val api: AuthInterface by lazy{
        Retrofit.Builder()
        .baseUrl(BASEURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthInterface::class.java)
    }
}


interface AuthInterface {
    @POST("register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse
    @POST("login")
    suspend fun login(@Body body:LoginRequest): RegisterResponse
}