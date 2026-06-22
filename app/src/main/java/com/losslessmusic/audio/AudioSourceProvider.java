package com.losslessmusic.audio;

import com.losslessmusic.models.Song;

import java.util.List;

public interface AudioSourceProvider {
    String getProviderName();
    Song.AudioSource getSourceType();
    boolean isAvailable();
    boolean isPremiumRequired();
    Song.AudioQuality getMaxQuality();

    void search(String query, SearchResultCallback callback);
    void getAlbumTracks(String albumId, SearchResultCallback callback);
    void getTrending(TrendingCallback callback);

    interface SearchResultCallback {
        void onResults(List<Song> songs);
        void onError(String message);
    }

    interface TrendingCallback {
        void onResults(List<Song> songs, String category);
        void onError(String message);
    }
}
