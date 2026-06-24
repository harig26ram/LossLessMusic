package com.losslessmusic.app.data.repository

import com.losslessmusic.app.data.api.InnerTubeApi
import com.losslessmusic.app.data.models.*
import com.losslessmusic.app.data.models.response.PlayerResponse

class MusicRepository {
    suspend fun search(query: String): Result<List<Song>> {
        return InnerTubeApi.search(query)
    }

    suspend fun getHomeFeed(): Result<List<HomeFeedSection>> {
        return InnerTubeApi.browse("FEmusic_home")
    }

    suspend fun getCharts(): Result<List<HomeFeedSection>> {
        return InnerTubeApi.browse("FEmusic_charts")
    }

    suspend fun getNewReleases(): Result<List<HomeFeedSection>> {
        return InnerTubeApi.browse("FEmusic_new_releases")
    }

    suspend fun getExplore(): Result<List<HomeFeedSection>> {
        return InnerTubeApi.browse("FEmusic_explore")
    }

    suspend fun getMoodCategories(): Result<List<MoodCategory>> {
        return InnerTubeApi.getMoodCategories()
    }

    suspend fun getStreamUrl(videoId: String): Result<PlayerResponse> {
        val result = InnerTubeApi.getStreamUrl(videoId)
        if (result.isFailure) {
            val piped = InnerTubeApi.getPipedStream(videoId)
            if (piped.isSuccess) {
                return Result.success(PlayerResponse(
                    playabilityStatus = PlayerResponse.PlayabilityStatus(status = "OK"),
                    streamingData = PlayerResponse.StreamingData(
                        adaptiveFormats = listOf(PlayerResponse.Format(
                            url = piped.getOrNull(), mimeType = "audio/mp4", bitrate = 128000, audioQuality = "AUDIO_QUALITY_MEDIUM"
                        ))
                    ),
                    videoDetails = null
                ))
            }
        }
        return result
    }
}
