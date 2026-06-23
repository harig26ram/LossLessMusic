package com.losslessmusic.app.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.losslessmusic.app.data.models.Song

@UnstableApi
class MusicPlayer(context: Context) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private var currentSong: Song? = null
    private val listeners = mutableListOf<Player.Listener>()

    val player: ExoPlayer get() = exoPlayer

    fun play(song: Song) {
        currentSong = song
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.streamUrl ?: return)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artists)
                    .build()
            )
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun isPlaying(): Boolean = exoPlayer.isPlaying

    fun currentPosition(): Long = exoPlayer.currentPosition

    fun duration(): Long = exoPlayer.duration

    fun release() {
        exoPlayer.release()
    }

    fun addListener(listener: Player.Listener) {
        listeners.add(listener)
        exoPlayer.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
        exoPlayer.removeListener(listener)
    }
}
