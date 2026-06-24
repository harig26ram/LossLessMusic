package com.losslessmusic.app.player

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.losslessmusic.app.domain.model.Song
import com.losslessmusic.app.diag.CrashLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayer @Inject constructor(
    private val context: android.content.Context
) {
    private val logger = CrashLogger
    private val _player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build(), true)
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _listeners = mutableListOf<Player.Listener>()
    var currentSong: Song? = null
        private set

    val player: ExoPlayer get() = _player

    fun play(song: Song) {
        try {
            currentSong = song
            val url = song.streamUrl ?: run {
                logger.e("Player", "No stream URL for ${song.title}")
                return
            }
            val mediaItem = MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(url)
                .setMediaMetadata(MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artists)
                    .build())
                .build()

            _player.stop()
            _player.clearMediaItems()
            _player.setMediaItem(mediaItem)
            _player.prepare()
            _player.play()
            logger.w("Player", "Playing: ${song.title}")
        } catch (e: Exception) {
            logger.e("Player", e)
        }
    }

    fun togglePlayPause() {
        try {
            if (_player.isPlaying) _player.pause() else _player.play()
        } catch (e: Exception) {
            logger.e("Player.toggle", e)
        }
    }

    fun seekTo(posMs: Long) {
        try { _player.seekTo(posMs) } catch (e: Exception) { logger.e("Player.seek", e) }
    }

    fun addListener(listener: Player.Listener) {
        _listeners.add(listener)
        _player.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        _listeners.remove(listener)
        _player.removeListener(listener)
    }

    fun isPlaying(): Boolean = _player.isPlaying
    fun currentPosition(): Long = _player.currentPosition
    fun duration(): Long = _player.duration

    fun release() {
        _player.release()
        _listeners.clear()
    }
}
