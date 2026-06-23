package com.losslessmusic.app.data.models

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class YouTubeLocale(
    val gl: String = Locale.getDefault().country,
    val hl: String = Locale.getDefault().toLanguageTag(),
)
