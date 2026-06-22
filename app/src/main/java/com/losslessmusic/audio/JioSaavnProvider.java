package com.losslessmusic.audio;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.losslessmusic.models.Song;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JioSaavnProvider implements AudioSourceProvider {

    private static final String API_BASE = "https://www.jiosaavn.com/api.php";

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public JioSaavnProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public String getProviderName() { return "JioSaavn"; }

    @Override
    public Song.AudioSource getSourceType() { return Song.AudioSource.JIOSAAVN; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public boolean isPremiumRequired() { return false; }

    @Override
    public Song.AudioQuality getMaxQuality() { return Song.AudioQuality.HIGH_320; }

    @Override
    public void search(String query, SearchResultCallback callback) {
        executor.execute(() -> {
            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                String url = API_BASE + "?__call=autocomplete.get&_format=json&_marker=0&cc=in&includeMetaTags=1&query=" + encoded;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseAutocompleteResults(body);
                        // Return results directly - they have enough info for display
                        mainHandler.post(() -> callback.onResults(songs));
                    } else {
                        mainHandler.post(() -> callback.onError("Search failed: " + response.code()));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("JioSaavn: " + e.getMessage()));
            }
        });
    }

    @Override
    public void getAlbumTracks(String albumId, SearchResultCallback callback) {
        executor.execute(() -> {
            try {
                String url = API_BASE + "?__call=playlist.getDetails&_format=json&_marker=0&cc=in&listid=" + albumId;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parsePlaylistTracks(body);
                        mainHandler.post(() -> callback.onResults(songs));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to load album"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    @Override
    public void getTrending(TrendingCallback callback) {
        executor.execute(() -> {
            try {
                // Use search for trending Tamil/Hindi songs
                String url = API_BASE + "?__call=autocomplete.get&_format=json&_marker=0&cc=in&query=trending";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseAutocompleteResults(body);
                        mainHandler.post(() -> callback.onResults(songs, "JioSaavn Trending"));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to load trending"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private List<Song> parseAutocompleteResults(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            if (response == null) return songs;

            JsonObject songsData = response.getAsJsonObject("songs");
            if (songsData != null) {
                JsonArray data = songsData.getAsJsonArray("data");
                if (data != null) {
                    for (int i = 0; i < Math.min(data.size(), 20); i++) {
                        try {
                            JsonObject item = data.get(i).getAsJsonObject();
                            Song song = parseSongFromJson(item);
                            if (song != null) songs.add(song);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return songs;
    }

    private List<Song> parsePlaylistTracks(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            if (response == null) return songs;

            JsonArray songsList = response.getAsJsonArray("songs");
            if (songsList != null) {
                for (int i = 0; i < songsList.size(); i++) {
                    try {
                        JsonObject item = songsList.get(i).getAsJsonObject();
                        Song song = parseSongFromJson(item);
                        if (song != null) songs.add(song);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return songs;
    }

    private Song parseSongFromJson(JsonObject item) {
        try {
            String id = "";
            if (item.has("id")) id = item.get("id").getAsString();

            String title = "Unknown";
            if (item.has("song")) title = item.get("song").getAsString();
            else if (item.has("title")) title = item.get("title").getAsString();

            String artist = "Unknown";
            if (item.has("primaryArtists")) {
                String a = item.get("primaryArtists").getAsString();
                if (!a.isEmpty()) artist = a;
            }
            if ("Unknown".equals(artist) && item.has("singers")) {
                String a = item.get("singers").getAsString();
                if (!a.isEmpty()) artist = a;
            }

            Song song = new Song(id, title, artist);
            song.setSource(Song.AudioSource.JIOSAAVN);
            song.setQuality(Song.AudioQuality.HIGH_320);

            // Get artwork
            if (item.has("image")) {
                try {
                    JsonArray images = item.getAsJsonArray("image");
                    if (images != null && images.size() > 0) {
                        JsonObject largest = images.get(images.size() - 1).getAsJsonObject();
                        if (largest.has("link")) {
                            song.setArtworkUrl(largest.get("link").getAsString());
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Get duration
            if (item.has("duration")) {
                try {
                    song.setDurationMs(Long.parseLong(item.get("duration").getAsString()) * 1000);
                } catch (NumberFormatException ignored) {}
            }

            // Get preview URL (30 sec preview - always works without auth)
            if (item.has("media_preview_url")) {
                song.setStreamUrl(item.get("media_preview_url").getAsString());
                song.setQuality(Song.AudioQuality.LOW_128);
            } else if (item.has("preview_url")) {
                song.setStreamUrl(item.get("preview_url").getAsString());
                song.setQuality(Song.AudioQuality.LOW_128);
            }

            return song;
        } catch (Exception e) {
            return null;
        }
    }
}
