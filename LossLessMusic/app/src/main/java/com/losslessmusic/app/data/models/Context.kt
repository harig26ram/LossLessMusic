package com.losslessmusic.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val osVersion: String?,
        val gl: String,
        val hl: String,
        val visitorData: String?,
    )
}
