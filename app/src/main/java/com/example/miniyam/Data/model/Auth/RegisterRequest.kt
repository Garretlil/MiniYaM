package com.example.miniyam.Data.model.Auth

data class RegisterRequest(
    val name:String,
    val email:String,
    val password:String
)