package com.losslessmusic.ui.player;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.bumptech.glide.Glide;
import com.losslessmusic.R;
import com.losslessmusic.databinding.ActivityPlayerBinding;
import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ExoPlayer exoPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean userSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("songs")) {
            playlist = intent.getParcelableArrayListExtra("songs");
            currentIndex = intent.getIntExtra("startIndex", 0);
        }

        setupToolbar();
        setupControls();
        initPlayer();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupControls() {
        binding.playPauseButton.setOnClickListener(v -> {
            if (exoPlayer != null) {
                if (exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                } else {
                    exoPlayer.play();
                }
                updatePlayPauseButton();
            }
        });

        binding.nextButton.setOnClickListener(v -> playNext());
        binding.prevButton.setOnClickListener(v -> playPrevious());

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) binding.currentTime.setText(formatTime(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { userSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (exoPlayer != null) exoPlayer.seekTo(seekBar.getProgress());
                userSeeking = false;
            }
        });

        binding.queueButton.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder("Queue (" + playlist.size() + "):\n");
            for (int i = 0; i < playlist.size(); i++) {
                Song s = playlist.get(i);
                sb.append(i == currentIndex ? "▶ " : "  ")
                  .append(s.getTitle()).append(" - ").append(s.getArtist()).append("\n");
            }
            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        });
    }

    private void initPlayer() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                            .header("Accept", "*/*")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        exoPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new ProgressiveMediaSource.Factory(
                        new OkHttpDataSource.Factory(client)))
                .setHandleAudioBecomingNoisy(true)
                .build();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this,
                        "Playback error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    currentIndex = exoPlayer.getCurrentMediaItemIndex();
                    updateUI();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlayPauseButton();
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    updateUI();
                }
            }
        });

        if (!playlist.isEmpty()) {
            buildQueue();
        }
    }

    private void buildQueue() {
        List<MediaItem> items = new ArrayList<>();
        for (Song song : playlist) {
            String url = song.getStreamUrl();
            if (url == null || url.isEmpty()) continue;

            MediaItem item = new MediaItem.Builder()
                    .setUri(url)
                    .setMediaMetadata(new MediaMetadata.Builder()
                            .setTitle(song.getTitle())
                            .setArtist(song.getArtist())
                            .setAlbumTitle(song.getAlbum())
                            .build())
                    .build();
            items.add(item);
        }

        if (items.isEmpty()) {
            Toast.makeText(this, "No playable songs", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        exoPlayer.setMediaItems(items, Math.min(currentIndex, items.size() - 1), 0);
        exoPlayer.prepare();
        exoPlayer.play();
        updateUI();

        Runnable updater = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
                if (exoPlayer != null && !exoPlayer.isPlaying()) {
                    updatePlayPauseButton();
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(updater);
    }

    private void playNext() {
        if (exoPlayer != null) exoPlayer.seekToNext();
    }

    private void playPrevious() {
        if (exoPlayer == null) return;
        if (exoPlayer.getCurrentPosition() > 3000) {
            exoPlayer.seekTo(0);
        } else {
            exoPlayer.seekToPrevious();
        }
    }

    private void updateUI() {
        if (currentIndex < 0 || currentIndex >= playlist.size()) return;
        Song current = playlist.get(currentIndex);

        binding.songTitle.setText(current.getTitle());
        binding.songArtist.setText(current.getArtist());
        binding.qualityInfo.setText(current.getSourceQualityLabel());

        Glide.with(this)
                .load(current.getArtworkUrl())
                .placeholder(R.drawable.ic_music_note)
                .centerCrop()
                .into(binding.albumArt);
    }

    private void updatePlayPauseButton() {
        if (exoPlayer == null) return;
        binding.playPauseButton.setImageResource(
                exoPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void updateSeekBar() {
        if (exoPlayer == null || userSeeking) return;
        long pos = exoPlayer.getCurrentPosition();
        long dur = exoPlayer.getDuration();
        if (dur > 0) {
            binding.seekBar.setMax((int) dur);
            binding.seekBar.setProgress((int) pos);
            binding.currentTime.setText(formatTime((int) pos));
            binding.totalTime.setText(formatTime((int) dur));
        }
    }

    private String formatTime(int millis) {
        int s = millis / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
