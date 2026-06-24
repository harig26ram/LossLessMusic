package com.losslessmusic.app.data.api

import com.losslessmusic.app.data.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context,
    val query: String? = null,
    val params: String? = null,
)

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String,
    val params: String? = null,
)

@Serializable
data class PlayerBody(
    val context: Context,
    val videoId: String,
    val contentCheckOk: Boolean = true,
    val racyCheckOk: Boolean = true,
)
