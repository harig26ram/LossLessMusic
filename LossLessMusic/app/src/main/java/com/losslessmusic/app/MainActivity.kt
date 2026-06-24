package com.losslessmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.losslessmusic.app.ui.components.MiniPlayer
import com.losslessmusic.app.ui.screens.*
import com.losslessmusic.app.ui.theme.LossLessMusicTheme
import com.losslessmusic.app.ui.theme.ThemeMode

enum class Screen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SETTINGS("Settings", Icons.Default.Settings),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MainViewModel = viewModel()
            val currentScreen = remember { mutableStateOf(Screen.HOME) }

            LossLessMusicTheme(themeMode = vm.themeMode) {
                if (vm.showPlayer && vm.currentSong != null) {
                    PlayerScreen(
                        song = vm.currentSong,
                        isPlaying = vm.isPlaying,
                        currentPosition = vm.currentPosition,
                        duration = vm.duration,
                        onTogglePlayPause = { vm.togglePlayPause() },
                        onSeek = { vm.seekTo(it) },
                        onBack = { vm.navigateToPlayer(false) },
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            Column {
                                if (vm.currentSong != null) {
                                    MiniPlayer(
                                        song = vm.currentSong,
                                        isPlaying = vm.isPlaying,
                                        onTogglePlayPause = { vm.togglePlayPause() },
                                        onClick = { vm.navigateToPlayer(true) },
                                    )
                                }
                                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                    Screen.entries.forEach { screen ->
                                        NavigationBarItem(
                                            selected = currentScreen.value == screen,
                                            onClick = {
                                                currentScreen.value = screen
                                                if (screen == Screen.HOME) vm.loadHomeFeed()
                                            },
                                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                                            label = { Text(screen.label, fontSize = if (currentScreen.value == screen) 12.sp else 11.sp, fontWeight = if (currentScreen.value == screen) FontWeight.SemiBold else FontWeight.Normal) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            when (currentScreen.value) {
                                Screen.HOME -> HomeScreen(
                                    homeSections = vm.homeSections,
                                    songs = vm.songs,
                                    isSearching = vm.isSearching,
                                    isLoadingHome = vm.isLoadingHome,
                                    error = vm.error,
                                    moodCategories = vm.moodCategories,
                                    onSearch = { vm.search(it) },
                                    onSongClick = { vm.playSong(it) },
                                    onAlbumClick = { },
                                    onArtistClick = { },
                                    onPlaylistClick = { },
                                    onMoodClick = { vm.loadHomeFeed() },
                                )
                                Screen.LIBRARY -> LibraryScreen()
                                Screen.SETTINGS -> SettingsScreen(
                                    themeMode = vm.themeMode,
                                    onThemeChange = { vm.updateTheme(it) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
