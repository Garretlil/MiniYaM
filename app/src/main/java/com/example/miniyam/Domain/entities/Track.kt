package com.example.miniyam.Domain.entities

data class Track(
    val id: Long = 0,
    val title: String,
    val artist: String,
    var duration: Int,
    val url: String,
    var imageUrl: String,
    val liked: Boolean,
)