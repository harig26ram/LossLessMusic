package com.losslessmusic.app.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.losslessmusic.app.data.api.InnerTubeApi
import com.losslessmusic.app.data.repository.MusicRepositoryImpl
import com.losslessmusic.app.domain.model.*
import com.losslessmusic.app.domain.repository.MusicRepository
import com.losslessmusic.app.diag.CrashLogger
import com.losslessmusic.app.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {
    private val logger = CrashLogger
    private val api = InnerTubeApi()
    private val repository: MusicRepository = MusicRepositoryImpl(api)
    val player = MusicPlayer(application)

    var songs by mutableStateOf<List<Song>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)
        private set
    var currentSong by mutableStateOf<Song?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var currentPosition by mutableLongStateOf(0L)
        private set
    var duration by mutableLongStateOf(0L)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var showFullPlayer by mutableStateOf(false)
        private set

    var homeSections by mutableStateOf<List<HomeFeedSection>>(emptyList())
        private set
    var isLoadingHome by mutableStateOf(false)
    var moodCategories by mutableStateOf<List<MoodCategory>>(emptyList())
    var themeMode = "DARK"

    private var positionJob: Job? = null
    private var initJob: Job? = null

    init {
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) startPositionUpdates() else stopPositionUpdates()
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == androidx.media3.common.Player.STATE_READY) {
                    duration = player.duration()
                }
            }
            override fun onPlayerError(err: androidx.media3.common.PlaybackException) {
                error = "Playback: ${err.localizedMessage}"
                logger.e("Player.Listener", err)
            }
        })
        initJob = viewModelScope.launch {
            loadHomeFeed()
            loadMoods()
        }
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            try {
                isLoadingHome = true; error = null
                delay(100)
                repository.getHomeFeed()
                    .onSuccess { homeSections = it; logger.w("HomeFeed", "Loaded ${it.size} sections") }
                    .onFailure { error = "Home: ${it.message}"; logger.e("HomeFeed", it) }
            } catch (e: Exception) {
                error = "Home load error"; logger.e("HomeFeed.load", e)
            } finally { isLoadingHome = false }
        }
    }

    private fun loadMoods() {
        viewModelScope.launch {
            try {
                repository.getMoodCategories()
                    .onSuccess { moodCategories = it }
                    .onFailure { logger.e("Moods.load", it) }
            } catch (e: Exception) { logger.e("Moods.load", e) }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                isSearching = true; error = null
                if (query.isBlank()) { songs = emptyList(); return@launch }
                repository.search(query)
                    .onSuccess { songs = it; logger.w("Search", "${it.size} results") }
                    .onFailure { error = "Search: ${it.message}"; logger.e("Search", it) }
            } catch (e: Exception) {
                error = "Search error"; logger.e("Search", e)
            } finally { isSearching = false }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                currentSong = song; error = null; showFullPlayer = true
                repository.getStreamUrl(song.id)
                    .onSuccess { url ->
                        val s = song.copy(streamUrl = url)
                        currentSong = s
                        player.play(s)
                    }
                    .onFailure { error = "Stream: ${it.message}"; logger.e("playSong", it) }
            } catch (e: Exception) {
                error = "Play error: ${e.message}"; logger.e("playSong", e)
            }
        }
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun seekTo(pos: Long) = player.seekTo(pos)
    fun setShowPlayer(show: Boolean) { showFullPlayer = show }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (true) {
                currentPosition = player.currentPosition()
                delay(200)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel(); positionJob = null
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
