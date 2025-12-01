package com.example.miniyam.Domain.repository

import com.example.miniyam.Data.model.Auth.LoginRequest
import com.example.miniyam.Data.model.Auth.RegisterRequest
import com.example.miniyam.Data.model.Auth.RegisterResponse

interface AuthRepository {
    suspend fun register(body: RegisterRequest): RegisterResponse
    suspend fun login(body: LoginRequest): RegisterResponse
}