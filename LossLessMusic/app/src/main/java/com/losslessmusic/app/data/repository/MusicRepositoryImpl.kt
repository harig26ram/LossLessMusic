package com.losslessmusic.app.data.repository

import com.losslessmusic.app.data.api.InnerTubeApi
import com.losslessmusic.app.domain.model.*
import com.losslessmusic.app.domain.repository.MusicRepository
import com.losslessmusic.app.diag.CrashLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val api: InnerTubeApi,
) : MusicRepository {

    private val logger = CrashLogger

    override suspend fun search(query: String): kotlin.Result<List<Song>> {
        return api.search(query)
    }

    override suspend fun getHomeFeed(): kotlin.Result<List<HomeFeedSection>> {
        return api.browse("FEmusic_home")
    }

    override suspend fun getMoodCategories(): kotlin.Result<List<MoodCategory>> {
        return api.getMoodCategories()
    }

    override suspend fun getMoodPlaylists(browseId: String, params: String?): kotlin.Result<List<Song>> {
        return api.browse(browseId, params).map { sections ->
            sections.flatMap { sec ->
                sec.items.filterIsInstance<HomeFeedItem.SongItem>().map { it.song }
            }
        }
    }

    override suspend fun getCharts(): kotlin.Result<List<HomeFeedSection>> {
        return api.browse("FEmusic_charts")
    }

    override suspend fun getNewReleases(): kotlin.Result<List<HomeFeedSection>> {
        return api.browse("FEmusic_new_releases")
    }

    override suspend fun getStreamUrl(videoId: String): kotlin.Result<String> {
        val result = api.getPlayer(videoId)
        if (result.isFailure) {
            logger.w("StreamUrl", result.exceptionOrNull()?.message ?: "fallback needed for $videoId")
        }
        return result
    }
}
