package com.losslessmusic.app.data.models.body

import com.losslessmusic.app.data.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(
    val context: Context,
    val videoId: String,
    val playlistId: String? = null,
    val contentCheckOk: Boolean = true,
)
