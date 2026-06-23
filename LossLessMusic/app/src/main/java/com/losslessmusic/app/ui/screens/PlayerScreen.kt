package com.losslessmusic.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import com.losslessmusic.app.data.models.Song
import com.losslessmusic.app.ui.theme.*

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
    if (song == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No song selected", color = TextTertiary, fontSize = 18.sp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("Now Playing", color = TextSecondary, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = song.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = song.title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = song.artists,
            color = TextSecondary,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
            onValueChange = { fraction -> onSeek((fraction * duration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = AccentGreen,
                activeTrackColor = AccentGreen,
                inactiveTrackColor = DarkSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDuration(currentPosition),
                color = TextTertiary,
                fontSize = 12.sp,
            )
            Text(
                text = formatDuration(duration),
                color = TextTertiary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { /* previous */ }) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(72.dp),
            ) {
                Icon(
                    if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = AccentGreen,
                    modifier = Modifier.size(72.dp),
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            IconButton(onClick = { /* next */ }) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${String.format("%02d", seconds)}"
}
