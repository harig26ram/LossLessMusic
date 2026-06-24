package com.losslessmusic.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.losslessmusic.app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeSections: List<HomeFeedSection>,
    songs: List<Song>,
    isSearching: Boolean,
    isLoadingHome: Boolean,
    error: String?,
    moodCategories: List<MoodCategory>,
    onSearch: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onMoodClick: (MoodCategory) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isSearchMode = query.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "LossLessMusic",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 48.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search songs...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = ""; onSearch("") }) {
                    Icon(Icons.Default.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                val q = query.trim()
                if (q.isNotEmpty()) { focusManager.clearFocus(); onSearch(q) }
            }),
            singleLine = true, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            isSearching -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, maxLines = 3)
                    Spacer(Modifier.height(8.dp))
                    FilledTonalButton(onClick = onSearch("")) { Text("Retry") }
                }
            }
            isSearchMode && songs.isNotEmpty() -> SearchResults(songs, onSongClick)
            isSearchMode && !isSearching -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> HomeContent(
                homeSections = homeSections,
                isLoading = isLoadingHome,
                moodCategories = moodCategories,
                onSongClick = onSongClick,
                onMoodClick = onMoodClick,
            )
        }
    }
}

@Composable
private fun HomeContent(
    homeSections: List<HomeFeedSection>,
    isLoading: Boolean,
    moodCategories: List<MoodCategory>,
    onSongClick: (Song) -> Unit,
    onMoodClick: (MoodCategory) -> Unit,
) {
    if (isLoading && homeSections.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        if (homeSections.isEmpty() && moodCategories.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Welcome!\nSearch for music to get started",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp)
                }
            }
        }

        for (section in homeSections) {
            item {
                Text(section.title, color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp))
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(section.items.take(20)) { item ->
                        when (item) {
                            is HomeFeedItem.SongItem -> MiniCard(
                                item.song.title, item.song.artists, item.song.thumbnailUrl,
                                onClick = { onSongClick(item.song) })
                            is HomeFeedItem.AlbumItem -> MiniCard(
                                item.album.title, item.album.artist ?: "Album", item.album.thumbnailUrl,
                                onClick = { })
                            is HomeFeedItem.ArtistItem -> RoundCard(
                                item.artist.name, item.artist.thumbnailUrl, onClick = { })
                            is HomeFeedItem.PlaylistItem -> MiniCard(
                                item.playlist.title, "Playlist", item.playlist.thumbnailUrl,
                                onClick = { })
                        }
                    }
                }
            }
        }

        if (moodCategories.isNotEmpty()) {
            item { SectionHeader("Moods & Genres") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(moodCategories) { mood ->
                        FilterChip(selected = false, onClick = { onMoodClick(mood) },
                            label = { Text(mood.title, fontSize = 13.sp) })
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun SearchResults(songs: List<Song>, onSongClick: (Song) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(songs, key = { it.id }) { song ->
            SongRow(song, onSongClick)
        }
    }
}

@Composable
fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (song.durationSeconds != null) {
            Text("%d:%02d".format(song.durationSeconds / 60, song.durationSeconds % 60),
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MiniCard(title: String, subtitle: String, imageUrl: String?, onClick: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable(onClick = onClick)) {
        AsyncImage(model = imageUrl, contentDescription = null,
            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop)
        Spacer(Modifier.height(6.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RoundCard(title: String, imageUrl: String?, onClick: () -> Unit) {
    Column(modifier = Modifier.width(100.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(model = imageUrl, contentDescription = null,
            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(50.dp)),
            contentScale = ContentScale.Crop)
        Spacer(Modifier.height(6.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, color = MaterialTheme.colorScheme.onBackground,
        fontSize = 20.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp))
}