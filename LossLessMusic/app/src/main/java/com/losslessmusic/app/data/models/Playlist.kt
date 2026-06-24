package com.losslessmusic.app.data.models

data class Playlist(
    val id: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val songCount: Int? = null,
    val author: String? = null,
)
