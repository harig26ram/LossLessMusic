package com.losslessmusic.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.losslessmusic.app.data.models.Song
import com.losslessmusic.app.data.repository.MusicRepository
import com.losslessmusic.app.player.MusicPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository()
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

    private var positionUpdateJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                this@MainViewModel.isPlaying = isPlaying
                if (isPlaying) startPositionUpdates()
                else stopPositionUpdates()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        duration = player.player.duration
                    }
                    Player.STATE_ENDED -> {
                        isPlaying = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                this@MainViewModel.error = "Playback error: ${error.localizedMessage}"
            }
        })
    }

    fun search(query: String) {
        if (query.isBlank()) {
            songs = emptyList()
            return
        }

        viewModelScope.launch {
            isSearching = true
            error = null
            val result = repository.search(query)
            result.onSuccess { songs = it }
            result.onFailure { error = "Search failed: ${it.message}" }
            isSearching = false
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            currentSong = song
            error = null

            val result = repository.getStreamUrl(song.id)
            result.onSuccess { playerResponse ->
                val streamUrl = playerResponse.streamingData?.adaptiveFormats
                    ?.filter { it.isAudio }
                    ?.maxByOrNull { it.bitrate ?: 0 }
                    ?.url

                if (streamUrl != null) {
                    val playableSong = song.copy(streamUrl = streamUrl)
                    currentSong = playableSong
                    player.play(playableSong)
                } else {
                    error = "No audio stream found"
                }
            }
            result.onFailure { e ->
                error = "Failed to get stream: ${e.message}"
            }
        }
    }

    fun togglePlayPause() {
        player.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                currentPosition = player.currentPosition()
                delay(200)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
