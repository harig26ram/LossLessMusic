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
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JioSaavnProvider implements AudioSourceProvider {

    private static final String API_BASE = "https://www.jiosaavn.com/api.php";
    private static final String CDN_BASE = "https://cdn.jiosaavn.com";

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public JioSaavnProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        executor = Executors.newFixedThreadPool(2);
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
                String url = API_BASE + "__call=autocomplete.get&_format=json&_marker=0&cc=in&includeMetaTags=1&query=" + encoded;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                        .addHeader("Accept", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseAutocompleteResults(body);
                        resolveSongDetails(songs, callback);
                    } else {
                        mainHandler.post(() -> callback.onError("Search failed: " + response.code()));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    @Override
    public void getAlbumTracks(String albumId, SearchResultCallback callback) {
        executor.execute(() -> {
            try {
                String url = API_BASE + "__call=playlist.getDetails&_format=json&_marker=0&cc=in&listid=" + albumId;

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
                String url = API_BASE + "__call=content.get&_format=json&_marker=0&cc=in&category=most-popular&lang=en";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseTrendingResults(body);
                        mainHandler.post(() -> callback.onResults(songs, "Most Popular"));
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
            JsonObject songsData = response.getAsJsonObject("songs");
            if (songsData != null) {
                JsonArray data = songsData.getAsJsonArray("data");
                if (data != null) {
                    for (int i = 0; i < Math.min(data.size(), 20); i++) {
                        JsonObject item = data.get(i).getAsJsonObject();
                        Song song = parseSongFromJson(item);
                        if (song != null) songs.add(song);
                    }
                }
            }
        } catch (Exception e) {
            // JSON parse error
        }
        return songs;
    }

    private List<Song> parseTrendingResults(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            JsonArray data = response.getAsJsonArray("data");
            if (data != null) {
                for (int i = 0; i < Math.min(data.size(), 30); i++) {
                    JsonObject item = data.get(i).getAsJsonObject();
                    Song song = parseSongFromJson(item);
                    if (song != null) songs.add(song);
                }
            }
        } catch (Exception e) {
            // JSON parse error
        }
        return songs;
    }

    private List<Song> parsePlaylistTracks(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            JsonArray songsList = response.getAsJsonArray("songs");
            if (songsList != null) {
                for (int i = 0; i < songsList.size(); i++) {
                    JsonObject item = songsList.get(i).getAsJsonObject();
                    Song song = parseSongFromJson(item);
                    if (song != null) songs.add(song);
                }
            }
        } catch (Exception e) {
            // JSON parse error
        }
        return songs;
    }

    private Song parseSongFromJson(JsonObject item) {
        try {
            String id = item.has("id") ? item.get("id").getAsString() : "";
            String title = item.has("song") ? item.get("song").getAsString() :
                    item.has("title") ? item.get("title").getAsString() : "Unknown";
            String artist = item.has("primaryArtists") ? item.get("primaryArtists").getAsString() :
                    item.has("singers") ? item.get("singers").getAsString() : "Unknown";
            String album = item.has("album") ? item.get("album").getAsString() : "";

            Song song = new Song(id, title, artist);
            song.setAlbum(album);
            song.setSource(Song.AudioSource.JIOSAAVN);

            if (item.has("image")) {
                JsonArray images = item.getAsJsonArray("image");
                if (images != null && images.size() > 0) {
                    JsonObject largest = images.get(images.size() - 1).getAsJsonObject();
                    if (largest.has("link")) {
                        song.setArtworkUrl(largest.get("link").getAsString());
                    }
                }
            }

            if (item.has("duration")) {
                try {
                    song.setDurationMs(Long.parseLong(item.get("duration").getAsString()) * 1000);
                } catch (NumberFormatException ignored) {}
            }

            if (item.has("perma_url")) {
                song.setAlbumId(item.get("perma_url").getAsString());
            }

            song.setQuality(Song.AudioQuality.HIGH_320);
            return song;
        } catch (Exception e) {
            return null;
        }
    }

    private void resolveSongDetails(List<Song> songs, SearchResultCallback callback) {
        if (songs.isEmpty()) {
            mainHandler.post(() -> callback.onResults(songs));
            return;
        }

        List<Song> resolved = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(songs.size());

        for (Song song : songs) {
            executor.execute(() -> {
                try {
                    String url = API_BASE + "__call=song.details&_format=json&_marker=0&cc=in&songid=" + song.getId();
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36")
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String body = response.body().string();
                            JsonObject data = gson.fromJson(body, JsonObject.class);
                            if (data != null && data.has("download_url")) {
                                song.setStreamUrl(data.get("download_url").getAsString());
                            } else if (data != null && data.has("media_preview_url")) {
                                song.setStreamUrl(data.get("media_preview_url").getAsString());
                                song.setQuality(Song.AudioQuality.LOW_128);
                            }
                            synchronized (resolved) {
                                resolved.add(song);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                if (remaining.decrementAndGet() == 0) {
                    mainHandler.post(() -> callback.onResults(resolved));
                }
            });
        }
    }
}
