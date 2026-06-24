package com.losslessmusic.app.data.models

data class Album(
    val id: String,
    val title: String,
    val artist: String? = null,
    val year: Int? = null,
    val thumbnailUrl: String? = null,
    val songCount: Int? = null,
)
