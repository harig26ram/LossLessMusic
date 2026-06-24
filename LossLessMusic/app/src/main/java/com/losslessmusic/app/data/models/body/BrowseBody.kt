package com.losslessmusic.app.data.models.body

import com.losslessmusic.app.data.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String,
    val params: String? = null,
)
