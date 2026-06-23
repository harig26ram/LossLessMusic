package com.losslessmusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.losslessmusic.app.data.models.Song
import com.losslessmusic.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    songs: List<Song>,
    isSearching: Boolean,
    error: String?,
    onSearch: (String) -> Unit,
    onSongClick: (Song) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Text(
            text = "LossLessMusic",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 48.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search songs, artists, albums...", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        onSearch("")
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch(query.trim())
                focusManager.clearFocus()
            }),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = DarkSurfaceVariant,
                cursorColor = AccentGreen,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGreen)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error, color = ErrorColor, fontSize = 14.sp, maxLines = 3)
            }
        } else if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Search for your favorite music", color = TextTertiary, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(songs, key = { it.id }) { song ->
                    SongCard(song = song, onClick = { onSongClick(song) })
                }
            }
        }
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(DarkSurface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artists,
                color = TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (song.duration != null) {
            Text(
                text = "${song.duration / 60}:${String.format("%02d", song.duration % 60)}",
                color = TextTertiary,
                fontSize = 12.sp,
            )
        }
    }
}
