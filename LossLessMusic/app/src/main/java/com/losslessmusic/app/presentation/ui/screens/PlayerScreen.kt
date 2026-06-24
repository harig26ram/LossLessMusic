package com.losslessmusic.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.losslessmusic.app.domain.model.Song

@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start).padding(8.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, "Minimize", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(32.dp))
            AsyncImage(
                model = song?.thumbnailUrl, contentDescription = null,
                modifier = Modifier.size(300.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(32.dp))
            Text(song?.title ?: "No track", color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 32.dp))
            Text(song?.artists ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp))
            Spacer(Modifier.height(24.dp))
            Slider(
                value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                onValueChange = { onSeek((it * duration).toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ), modifier = Modifier.padding(horizontal = 32.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(currentPosition), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(formatTime(duration), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { }) { Icon(Icons.Default.SkipPrevious, "Previous", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(40.dp)) }
                Spacer(Modifier.width(32.dp))
                FilledIconButton(onClick = onTogglePlayPause, modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (isPlaying) "Pause" else "Play", modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.width(32.dp))
                IconButton(onClick = { }) { Icon(Icons.Default.SkipNext, "Next", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(40.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                IconButton(onClick = { }) { Icon(Icons.Default.Shuffle, "Shuffle", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = { }) { Icon(Icons.Default.Repeat, "Repeat", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = { }) { Icon(Icons.Default.QueueMusic, "Queue", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "0:00"
    val total = millis / 1000
    return "%d:%02d".format(total / 60, total % 60)
}