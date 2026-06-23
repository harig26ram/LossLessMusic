package com.losslessmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.losslessmusic.app.ui.screens.HomeScreen
import com.losslessmusic.app.ui.screens.PlayerScreen
import com.losslessmusic.app.ui.theme.LossLessMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LossLessMusicTheme {
                val viewModel: MainViewModel = viewModel()
                var showPlayer by remember { mutableStateOf(false) }

                if (showPlayer && viewModel.currentSong != null) {
                    PlayerScreen(
                        song = viewModel.currentSong,
                        isPlaying = viewModel.isPlaying,
                        currentPosition = viewModel.currentPosition,
                        duration = viewModel.duration,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSeek = { viewModel.seekTo(it) },
                        onBack = { showPlayer = false },
                    )
                } else {
                    HomeScreen(
                        songs = viewModel.songs,
                        isSearching = viewModel.isSearching,
                        onSearch = { viewModel.search(it) },
                        onSongClick = { song ->
                            viewModel.playSong(song)
                            showPlayer = true
                        },
                    )
                }
            }
        }
    }
}
