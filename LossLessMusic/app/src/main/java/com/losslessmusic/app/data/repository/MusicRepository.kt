package com.losslessmusic.app.data.repository

import com.losslessmusic.app.data.api.InnerTubeApi
import com.losslessmusic.app.data.models.Song
import com.losslessmusic.app.data.models.response.PlayerResponse

class MusicRepository {
    suspend fun search(query: String): Result<List<Song>> {
        return InnerTubeApi.search(query)
    }

    suspend fun getStreamUrl(videoId: String): Result<PlayerResponse> {
        val result = InnerTubeApi.getStreamUrl(videoId)
        if (result.isFailure) {
            // Fallback to Piped API
            val pipedUrl = InnerTubeApi.getPipedStream(videoId)
            if (pipedUrl.isSuccess) {
                return Result.success(
                    PlayerResponse(
                        playabilityStatus = PlayerResponse.PlayabilityStatus(status = "OK"),
                        streamingData = PlayerResponse.StreamingData(
                            adaptiveFormats = listOf(
                                PlayerResponse.Format(
                                    url = pipedUrl.getOrNull(),
                                    mimeType = "audio/mp4",
                                    bitrate = 128000,
                                    audioQuality = "AUDIO_QUALITY_MEDIUM"
                                )
                            )
                        ),
                        videoDetails = null
                    )
                )
            }
        }
        return result
    }
}
