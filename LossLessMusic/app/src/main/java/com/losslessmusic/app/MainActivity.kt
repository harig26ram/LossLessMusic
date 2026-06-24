package com.losslessmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.losslessmusic.app.presentation.ui.components.MiniPlayer
import com.losslessmusic.app.presentation.ui.screens.HomeScreen
import com.losslessmusic.app.presentation.ui.screens.LibraryScreen
import com.losslessmusic.app.presentation.ui.screens.PlayerScreen
import com.losslessmusic.app.presentation.ui.screens.SettingsScreen
import com.losslessmusic.app.presentation.ui.theme.LossLessMusicTheme
import com.losslessmusic.app.presentation.ui.theme.ThemeMode
import com.losslessmusic.app.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            LossLessMusicTheme(
                themeMode = when (viewModel.themeMode) {
                    "LIGHT" -> ThemeMode.LIGHT
                    "DYNAMIC" -> ThemeMode.DYNAMIC
                    else -> ThemeMode.DARK
                }
            ) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

enum class BottomTab { HOME, LIBRARY, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(BottomTab.HOME) }

    val songs = viewModel.songs
    val isSearching = viewModel.isSearching
    val homeSections = viewModel.homeSections
    val isLoadingHome = viewModel.isLoadingHome
    val error = viewModel.error
    val moodCategories = viewModel.moodCategories
    val currentSong = viewModel.currentSong
    val isPlaying = viewModel.isPlaying

    Scaffold(
        bottomBar = {
            if (!viewModel.showFullPlayer) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") },
                        selected = currentTab == BottomTab.HOME,
                        onClick = { currentTab = BottomTab.HOME }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LibraryMusic, "Library") },
                        label = { Text("Library") },
                        selected = currentTab == BottomTab.LIBRARY,
                        onClick = { currentTab = BottomTab.LIBRARY }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, "Settings") },
                        label = { Text("Settings") },
                        selected = currentTab == BottomTab.SETTINGS,
                        onClick = { currentTab = BottomTab.SETTINGS }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                BottomTab.HOME -> HomeScreen(
                    homeSections = homeSections,
                    songs = songs,
                    isSearching = isSearching,
                    isLoadingHome = isLoadingHome,
                    error = error,
                    moodCategories = moodCategories,
                    onSearch = { viewModel.search(it) },
                    onSongClick = { viewModel.playSong(it) },
                    onMoodClick = { },
                )
                BottomTab.LIBRARY -> LibraryScreen()
                BottomTab.SETTINGS -> SettingsScreen(
                    themeMode = viewModel.themeMode,
                    onThemeChange = { }
                )
            }

            AnimatedVisibility(
                visible = viewModel.showFullPlayer,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                PlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPosition = viewModel.currentPosition,
                    duration = viewModel.duration,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekTo(it) },
                    onBack = { viewModel.setShowPlayer(false) },
                )
            }
        }

        if (!viewModel.showFullPlayer) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shadowElevation = NavigationBarDefaults.Elevation
            ) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onClick = { viewModel.setShowPlayer(true) },
                )
            }
        }
    }
}
