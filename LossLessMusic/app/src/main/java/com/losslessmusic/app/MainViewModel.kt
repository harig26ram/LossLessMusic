package com.losslessmusic.app

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.losslessmusic.app.data.models.*
import com.losslessmusic.app.data.repository.MusicRepository
import com.losslessmusic.app.player.MusicPlayer
import com.losslessmusic.app.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository()
    val player = MusicPlayer(application)

    // Player state
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
    var showPlayer by mutableStateOf(false)
        private set

    // Home feed
    var homeSections by mutableStateOf<List<HomeFeedSection>>(emptyList())
        private set
    var isLoadingHome by mutableStateOf(false)
        private set

    // Moods
    var moodCategories by mutableStateOf<List<MoodCategory>>(emptyList())
        private set
    var isLoadingMoods by mutableStateOf(false)
        private set

    // Charts
    var chartSections by mutableStateOf<List<HomeFeedSection>>(emptyList())
        private set
    var isLoadingCharts by mutableStateOf(false)
        private set

    // New releases
    var newReleaseSections by mutableStateOf<List<HomeFeedSection>>(emptyList())
        private set
    var isLoadingNewReleases by mutableStateOf(false)
        private set

    // Settings
    var themeMode by mutableStateOf(ThemeMode.DARK)
        private set

    // Queue
    private val songQueue = mutableListOf<Song>()
    private var queueIndex = -1

    private var positionUpdateJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) startPositionUpdates() else stopPositionUpdates()
            }
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> duration = player.player.duration
                    Player.STATE_ENDED -> { isPlaying = false; playNext() }
                }
            }
            override fun onPlayerError(err: PlaybackException) {
                error = "Playback error: ${err.localizedMessage}"
            }
        })
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            try {
                isLoadingHome = true
                error = null
                repository.getHomeFeed().onSuccess { homeSections = it }
                    .onFailure { Log.e("VM", "Home feed failed", it); error = "Home: ${it.message}" }
            } catch (e: Exception) { error = "Home error: ${e.message}" }
            finally { isLoadingHome = false }
        }
    }

    fun loadCharts() {
        viewModelScope.launch {
            try {
                isLoadingCharts = true; error = null
                repository.getCharts().onSuccess { chartSections = it }
                    .onFailure { error = "Charts: ${it.message}" }
            } catch (e: Exception) { error = "Charts error: ${e.message}" }
            finally { isLoadingCharts = false }
        }
    }

    fun loadNewReleases() {
        viewModelScope.launch {
            try {
                isLoadingNewReleases = true; error = null
                repository.getNewReleases().onSuccess { newReleaseSections = it }
                    .onFailure { error = "New: ${it.message}" }
            } catch (e: Exception) { error = "New releases error: ${e.message}" }
            finally { isLoadingNewReleases = false }
        }
    }

    fun loadMoodCategories() {
        viewModelScope.launch {
            try {
                isLoadingMoods = true; error = null
                repository.getMoodCategories().onSuccess { moodCategories = it }
                    .onFailure { error = "Moods: ${it.message}" }
            } catch (e: Exception) { error = "Moods error: ${e.message}" }
            finally { isLoadingMoods = false }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) { songs = emptyList(); return }
        viewModelScope.launch {
            try {
                isSearching = true; error = null
                repository.search(query).onSuccess { songs = it }
                    .onFailure { error = "Search: ${it.message}" }
            } catch (e: Exception) { error = "Search error: ${e.message}" }
            finally { isSearching = false }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                currentSong = song; error = null; showPlayer = true
                val result = repository.getStreamUrl(song.id)
                result.onSuccess { pr ->
                    val url = pr.streamingData?.adaptiveFormats?.filter { it.isAudio }?.maxByOrNull { it.bitrate ?: 0 }?.url
                    if (url != null) {
                        currentSong = song.copy(streamUrl = url)
                        player.play(currentSong!!)
                    } else error = "No audio stream found"
                }.onFailure { error = "Stream: ${it.message}" }
            } catch (e: Exception) { error = "Play error: ${e.message}" }
        }
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun seekTo(pos: Long) = player.seekTo(pos)
    fun navigateToPlayer(show: Boolean) { showPlayer = show }
    fun updateTheme(mode: ThemeMode) { themeMode = mode }

    private fun playNext() {
        if (queueIndex < songQueue.size - 1) {
            queueIndex++
            playSong(songQueue[queueIndex])
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) { currentPosition = player.currentPosition(); delay(200) }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel(); positionUpdateJob = null
    }

    override fun onCleared() { super.onCleared(); player.release() }
}
