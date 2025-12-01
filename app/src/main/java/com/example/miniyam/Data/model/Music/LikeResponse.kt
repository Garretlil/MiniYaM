package com.example.miniyam.Data.model.Music

data class LikeResponse(
    val trackId: Long,
    val message: String,
    val liked: Boolean,
)