package com.losslessmusic.app.data.models

data class Song(
    val id: String,
    val title: String,
    val artists: String,
    val album: String? = null,
    val duration: Int? = null,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
    val contentLength: Long? = null,
    val mimeType: String? = null,
)
