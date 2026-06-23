package com.losslessmusic.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.losslessmusic.LossLessApp;
import com.losslessmusic.MainActivity;
import com.losslessmusic.R;
import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

@UnstableApi
public class PlaybackService extends Service {

    private static final String TAG = "PlaybackService";
    private static final String ACTION_PLAY = "com.losslessmusic.PLAY";
    private static final String ACTION_PAUSE = "com.losslessmusic.PAUSE";
    private static final String ACTION_NEXT = "com.losslessmusic.NEXT";
    private static final String ACTION_PREV = "com.losslessmusic.PREV";
    private static final String ACTION_STOP = "com.losslessmusic.STOP";
    private static final String NOTIFICATION_ID = "lossless_playback";
    private static final int NOTIFICATION_CODE = 1;

    private ExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private final IBinder binder = new LocalBinder();
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = 0;

    private Player.Listener playerListener;
    private OnPlaybackErrorListener errorListener;

    public interface OnPlaybackErrorListener {
        void onPlaybackError(String error);
    }

    public void setOnPlaybackErrorListener(OnPlaybackErrorListener listener) {
        this.errorListener = listener;
    }

    public class LocalBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
        initMediaSession();
    }

    private void initPlayer() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                            .header("Accept", "*/*")
                            .header("Accept-Language", "en-US,en;q=0.9,hi;q=0.8")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        OkHttpDataSource.Factory dataSourceFactory =
                new OkHttpDataSource.Factory(client);

        exoPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new ProgressiveMediaSource.Factory(dataSourceFactory))
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build();

        playerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    updateNotification();
                } else if (state == Player.STATE_IDLE) {
                    Log.w(TAG, "Player state: IDLE");
                } else if (state == Player.STATE_BUFFERING) {
                    Log.d(TAG, "Player state: BUFFERING");
                } else if (state == Player.STATE_ENDED) {
                    Log.d(TAG, "Player state: ENDED");
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                String msg = "Playback error: " + error.getMessage();
                Log.e(TAG, msg, error);
                if (errorListener != null) {
                    errorListener.onPlaybackError(msg);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updateNotification();
            }

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    currentIndex = exoPlayer.getCurrentMediaItemIndex();
                }
            }
        };

        exoPlayer.addListener(playerListener);
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "LossLessMusic");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                exoPlayer.play();
            }

            @Override
            public void onPause() {
                exoPlayer.pause();
            }

            @Override
            public void onSkipToNext() {
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                playPrevious();
            }

            @Override
            public void onStop() {
                exoPlayer.stop();
                stopForeground(true);
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                exoPlayer.seekTo(pos);
            }
        });
        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.hasExtra("songs")) {
                ArrayList<Song> songs = intent.getParcelableArrayListExtra("songs");
                if (songs != null && !songs.isEmpty()) {
                    int startIndex = intent.getIntExtra("startIndex", 0);
                    playQueue(songs, startIndex);
                }
            }
            switch (intent.getAction()) {
                case ACTION_PLAY:
                    exoPlayer.play();
                    break;
                case ACTION_PAUSE:
                    exoPlayer.pause();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREV:
                    playPrevious();
                    break;
                case ACTION_STOP:
                    exoPlayer.stop();
                    stopForeground(true);
                    stopSelf();
                    return START_NOT_STICKY;
            }
        }
        return START_STICKY;
    }

    public void playSong(Song song) {
        List<Song> singleList = new ArrayList<>();
        singleList.add(song);
        playQueue(singleList, 0);
    }

    public void playQueue(List<Song> songs, int startIndex) {
        playlist.clear();
        playlist.addAll(songs);
        currentIndex = startIndex;

        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            MediaItem.Builder builder = new MediaItem.Builder();

            if (song.getLocalUri() != null) {
                builder.setUri(song.getLocalUri());
            } else if (song.getStreamUrl() != null && !song.getStreamUrl().isEmpty()) {
                builder.setUri(song.getStreamUrl());
            } else {
                Log.w(TAG, "Song has no playable URL: " + song.getTitle());
                continue;
            }

            MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
            metadataBuilder.setTitle(song.getTitle());
            metadataBuilder.setArtist(song.getArtist());
            metadataBuilder.setAlbumTitle(song.getAlbum());
            builder.setMediaMetadata(metadataBuilder.build());

            mediaItems.add(builder.build());
        }

        if (mediaItems.isEmpty()) {
            Log.e(TAG, "No playable media items");
            if (errorListener != null) {
                errorListener.onPlaybackError("No playable source available");
            }
            return;
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, 0);
        exoPlayer.prepare();
        exoPlayer.play();
        startForeground(NOTIFICATION_CODE, buildNotification());
    }

    public void playNext() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        exoPlayer.seekToNext();
    }

    public void playPrevious() {
        if (playlist.isEmpty()) return;
        if (exoPlayer.getCurrentPosition() > 3000) {
            exoPlayer.seekTo(0);
        } else {
            currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
            exoPlayer.seekToPrevious();
        }
    }

    public void seekTo(long positionMs) {
        exoPlayer.seekTo(positionMs);
    }

    public void togglePlayPause() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        } else {
            exoPlayer.play();
        }
    }

    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    public long getCurrentPosition() {
        return exoPlayer != null ? exoPlayer.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return exoPlayer != null ? exoPlayer.getDuration() : 0;
    }

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public Song getCurrentSong() {
        if (playlist.isEmpty() || currentIndex >= playlist.size()) return null;
        return playlist.get(currentIndex);
    }

    public List<Song> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    private Notification buildNotification() {
        Song current = getCurrentSong();
        String title = current != null ? current.getTitle() : "Unknown";
        String artist = current != null ? current.getArtist() : "";

        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingLaunch = PendingIntent.getActivity(
                this, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, LossLessApp.CHANNEL_PLAYBACK)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(pendingLaunch)
                .setOngoing(true);

        builder.addAction(R.drawable.ic_skip_previous, "Previous",
                createActionIntent(ACTION_PREV));
        if (isPlaying()) {
            builder.addAction(R.drawable.ic_pause, "Pause",
                    createActionIntent(ACTION_PAUSE));
        } else {
            builder.addAction(R.drawable.ic_play, "Play",
                    createActionIntent(ACTION_PLAY));
        }
        builder.addAction(R.drawable.ic_skip_next, "Next",
                createActionIntent(ACTION_NEXT));

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));

        return builder.build();
    }

    private PendingIntent createActionIntent(String action) {
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(action);
        return PendingIntent.getService(
                this, action.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void updateNotification() {
        Notification notification = buildNotification();
        android.app.NotificationManager manager =
                getSystemService(android.app.NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_CODE, notification);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
            exoPlayer.release();
        }
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
    }
}
