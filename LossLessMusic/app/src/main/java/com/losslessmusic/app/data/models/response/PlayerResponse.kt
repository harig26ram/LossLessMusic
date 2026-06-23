package com.losslessmusic.app.data.models.response

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResponse(
    val playabilityStatus: PlayabilityStatus? = null,
    val streamingData: StreamingData? = null,
    val videoDetails: VideoDetails? = null,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String? = null,
        val reason: String? = null,
    )

    @Serializable
    data class StreamingData(
        val formats: List<Format>? = null,
        val adaptiveFormats: List<Format>? = null,
        val expiresInSeconds: Int? = null,
    )

    @Serializable
    data class Format(
        val itag: Int? = null,
        val url: String? = null,
        val mimeType: String? = null,
        val bitrate: Int? = null,
        val width: Int? = null,
        val height: Int? = null,
        val contentLength: Long? = null,
        val quality: String? = null,
        val averageBitrate: Int? = null,
        val audioQuality: String? = null,
        val approxDurationMs: String? = null,
        val audioSampleRate: Int? = null,
        val audioChannels: Int? = null,
        val loudnessDb: Double? = null,
        val lastModified: Long? = null,
    ) {
        val isAudio: Boolean get() = width == null || width == 0
    }

    @Serializable
    data class VideoDetails(
        val videoId: String? = null,
        val title: String? = null,
        val author: String? = null,
        val channelId: String? = null,
        val lengthSeconds: String? = null,
        val musicVideoType: String? = null,
        val thumbnail: ThumbnailResponse? = null,
    )

    @Serializable
    data class ThumbnailResponse(
        val thumbnails: List<ThumbnailItem>? = null,
    )

    @Serializable
    data class ThumbnailItem(
        val url: String? = null,
        val width: Int? = null,
        val height: Int? = null,
    )
}
