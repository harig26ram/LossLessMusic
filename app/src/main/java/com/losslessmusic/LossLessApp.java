package com.losslessmusic;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class LossLessApp extends Application {

    public static final String CHANNEL_PLAYBACK = "lossless_playback";
    public static final String CHANNEL_PROGRESS = "lossless_progress";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel playbackChannel = new NotificationChannel(
                    CHANNEL_PLAYBACK,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            playbackChannel.setDescription("Controls for music playback");
            playbackChannel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(playbackChannel);
            }
        }
    }
}
