package com.losslessmusic.app.domain.usecase

import com.losslessmusic.app.domain.model.*
import com.losslessmusic.app.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(query: String): kotlin.Result<List<Song>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext kotlin.Result.success(emptyList())
        repository.search(query)
    }
}

class GetHomeFeedUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(): kotlin.Result<List<HomeFeedSection>> = withContext(Dispatchers.IO) {
        repository.getHomeFeed()
    }
}

class GetMoodCategoriesUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(): kotlin.Result<List<MoodCategory>> = withContext(Dispatchers.IO) {
        repository.getMoodCategories()
    }
}

class GetMoodPlaylistsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(browseId: String, params: String?): kotlin.Result<List<Song>> = withContext(Dispatchers.IO) {
        repository.getMoodPlaylists(browseId, params)
    }
}

class GetChartsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(): kotlin.Result<List<HomeFeedSection>> = withContext(Dispatchers.IO) {
        repository.getCharts()
    }
}

class GetNewReleasesUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(): kotlin.Result<List<HomeFeedSection>> = withContext(Dispatchers.IO) {
        repository.getNewReleases()
    }
}

class GetStreamUrlUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(videoId: String): kotlin.Result<String> = withContext(Dispatchers.IO) {
        repository.getStreamUrl(videoId)
    }
}
