package com.losslessmusic.app.domain.repository

import com.losslessmusic.app.domain.model.*

interface MusicRepository {
    suspend fun search(query: String): Result<List<Song>>
    suspend fun getHomeFeed(): Result<List<HomeFeedSection>>
    suspend fun getMoodCategories(): Result<List<MoodCategory>>
    suspend fun getMoodPlaylists(browseId: String, params: String?): Result<List<Song>>
    suspend fun getCharts(): Result<List<HomeFeedSection>>
    suspend fun getNewReleases(): Result<List<HomeFeedSection>>
    suspend fun getStreamUrl(videoId: String): Result<String>
}