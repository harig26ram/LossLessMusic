package com.losslessmusic.ui.player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import com.bumptech.glide.Glide;
import com.losslessmusic.R;
import com.losslessmusic.audio.PlaybackService;
import com.losslessmusic.databinding.ActivityPlayerBinding;
import com.losslessmusic.models.Song;

import java.util.List;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private PlaybackService playbackService;
    private boolean serviceBound = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean userSeeking = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            serviceBound = true;

            playbackService.setOnPlaybackErrorListener(error -> {
                handler.post(() -> {
                    Toast.makeText(PlayerActivity.this, error, Toast.LENGTH_LONG).show();
                    binding.qualityInfo.setText("Error: " + error);
                });
            });

            updateUI();
            startProgressUpdater();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupControls();
        bindPlaybackService();
    }

    private void setupControls() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.playPauseButton.setOnClickListener(v -> {
            if (serviceBound) {
                playbackService.togglePlayPause();
                updatePlayPauseButton();
            }
        });

        binding.nextButton.setOnClickListener(v -> {
            if (serviceBound) playbackService.playNext();
        });

        binding.prevButton.setOnClickListener(v -> {
            if (serviceBound) playbackService.playPrevious();
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && serviceBound) {
                    binding.currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (serviceBound) {
                    playbackService.seekTo(seekBar.getProgress());
                }
                userSeeking = false;
            }
        });

        binding.queueButton.setOnClickListener(v -> {
            if (serviceBound) {
                List<Song> playlist = playbackService.getPlaylist();
                StringBuilder sb = new StringBuilder("Queue (" + playlist.size() + " songs):\n");
                for (int i = 0; i < playlist.size(); i++) {
                    Song s = playlist.get(i);
                    String marker = (i == playbackService.getCurrentIndex()) ? "▶ " : "  ";
                    sb.append(marker).append(s.getTitle()).append(" - ").append(s.getArtist()).append("\n");
                }
                Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindPlaybackService() {
        Intent intent = new Intent(this, PlaybackService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void updateUI() {
        if (!serviceBound) return;

        Song current = playbackService.getCurrentSong();
        if (current != null) {
            binding.songTitle.setText(current.getTitle());
            binding.songArtist.setText(current.getArtist());
            binding.qualityInfo.setText(current.getSourceQualityLabel());

            Glide.with(this)
                    .load(current.getArtworkUrl())
                    .placeholder(R.drawable.ic_music_note)
                    .centerCrop()
                    .into(binding.albumArt);
        } else {
            binding.songTitle.setText("No song playing");
            binding.songArtist.setText("");
            binding.qualityInfo.setText("");
        }

        updatePlayPauseButton();
        updateSeekBar();
    }

    private void updatePlayPauseButton() {
        if (serviceBound && playbackService.isPlaying()) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause);
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void updateSeekBar() {
        if (!serviceBound || userSeeking) return;

        long position = playbackService.getCurrentPosition();
        long duration = playbackService.getDuration();

        if (duration > 0) {
            binding.seekBar.setMax((int) duration);
            binding.seekBar.setProgress((int) position);
            binding.currentTime.setText(formatTime((int) position));
            binding.totalTime.setText(formatTime((int) duration));
        }
    }

    private void startProgressUpdater() {
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                if (serviceBound) {
                    updateSeekBar();
                    updatePlayPauseButton();
                    updateUI();
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(updater);
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}
