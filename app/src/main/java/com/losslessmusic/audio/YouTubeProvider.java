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

public class YouTubeProvider implements AudioSourceProvider {

    private static final String[] INV_INSTANCES = {
            "https://inv.nadeko.net",
            "https://invidious.nerdvpn.de",
            "https://yt.artemislena.eu",
            "https://invidious.privacyredirect.com"
    };

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public YouTubeProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        executor = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public String getProviderName() { return "YouTube Music"; }

    @Override
    public Song.AudioSource getSourceType() { return Song.AudioSource.YOUTUBE_MUSIC; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public boolean isPremiumRequired() { return false; }

    @Override
    public Song.AudioQuality getMaxQuality() { return Song.AudioQuality.MEDIUM_256; }

    @Override
    public void search(String query, SearchResultCallback callback) {
        executor.execute(() -> {
            List<Song> allResults = new ArrayList<>();
            for (String instance : INV_INSTANCES) {
                try {
                    String encoded = URLEncoder.encode(query, "UTF-8");
                    String url = instance + "/api/v1/search?q=" + encoded + "&type=video&sort_by=relevance";

                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", "LossLessMusic/1.0")
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String body = response.body().string();
                            List<Song> songs = parseSearchResults(body);
                            if (!songs.isEmpty()) {
                                allResults.addAll(songs);
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (allResults.isEmpty()) {
                mainHandler.post(() -> callback.onError("Search failed"));
            } else {
                resolveAudioStreams(allResults, callback);
            }
        });
    }

    @Override
    public void getAlbumTracks(String albumId, SearchResultCallback callback) {
        executor.execute(() -> {
            for (String instance : INV_INSTANCES) {
                try {
                    String url = instance + "/api/v1/playlists/" + albumId;
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", "LossLessMusic/1.0")
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String body = response.body().string();
                            List<Song> songs = parsePlaylistResults(body);
                            mainHandler.post(() -> callback.onResults(songs));
                            return;
                        }
                    }
                } catch (Exception ignored) {}
            }
            mainHandler.post(() -> callback.onError("Failed to load playlist"));
        });
    }

    @Override
    public void getTrending(TrendingCallback callback) {
        executor.execute(() -> {
            for (String instance : INV_INSTANCES) {
                try {
                    String url = instance + "/api/v1/trending?region=IN";

                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", "LossLessMusic/1.0")
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String body = response.body().string();
                            List<Song> songs = parseTrendingResults(body);
                            if (!songs.isEmpty()) {
                                mainHandler.post(() -> callback.onResults(songs, "YouTube Trending"));
                                return;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
            mainHandler.post(() -> callback.onError("Failed to load trending"));
        });
    }

    private List<Song> parseSearchResults(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonArray results = gson.fromJson(json, JsonArray.class);
            if (results != null) {
                for (int i = 0; i < Math.min(results.size(), 20); i++) {
                    JsonObject item = results.get(i).getAsJsonObject();
                    String type = item.has("type") ? item.get("type").getAsString() : "";
                    if (!"video".equals(type)) continue;

                    String id = item.has("videoId") ? item.get("videoId").getAsString() : "";
                    String title = item.has("title") ? item.get("title").getAsString() : "Unknown";
                    String author = item.has("author") ? item.get("author").getAsString() : "Unknown";

                    if (id.isEmpty()) continue;

                    Song song = new Song(id, title, author);
                    song.setSource(Song.AudioSource.YOUTUBE_MUSIC);
                    song.setQuality(Song.AudioQuality.MEDIUM_256);

                    if (item.has("videoThumbnails")) {
                        JsonArray thumbs = item.getAsJsonArray("videoThumbnails");
                        if (thumbs != null && thumbs.size() > 0) {
                            JsonObject thumb = thumbs.get(thumbs.size() - 1).getAsJsonObject();
                            if (thumb.has("url")) {
                                song.setArtworkUrl(thumb.get("url").getAsString());
                            }
                        }
                    }

                    if (item.has("lengthSeconds")) {
                        try {
                            song.setDurationMs(Long.parseLong(item.get("lengthSeconds").getAsString()) * 1000);
                        } catch (NumberFormatException ignored) {}
                    }

                    songs.add(song);
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
            JsonArray results = gson.fromJson(json, JsonArray.class);
            if (results != null) {
                for (int i = 0; i < Math.min(results.size(), 30); i++) {
                    JsonObject item = results.get(i).getAsJsonObject();
                    String id = item.has("videoId") ? item.get("videoId").getAsString() : "";
                    String title = item.has("title") ? item.get("title").getAsString() : "Unknown";
                    String author = item.has("author") ? item.get("author").getAsString() : "Unknown";

                    if (id.isEmpty()) continue;

                    Song song = new Song(id, title, author);
                    song.setSource(Song.AudioSource.YOUTUBE_MUSIC);
                    song.setQuality(Song.AudioQuality.MEDIUM_256);

                    if (item.has("videoThumbnails")) {
                        JsonArray thumbs = item.getAsJsonArray("videoThumbnails");
                        if (thumbs != null && thumbs.size() > 0) {
                            JsonObject thumb = thumbs.get(thumbs.size() - 1).getAsJsonObject();
                            if (thumb.has("url")) {
                                song.setArtworkUrl(thumb.get("url").getAsString());
                            }
                        }
                    }

                    if (item.has("lengthSeconds")) {
                        try {
                            song.setDurationMs(Long.parseLong(item.get("lengthSeconds").getAsString()) * 1000);
                        } catch (NumberFormatException ignored) {}
                    }

                    songs.add(song);
                }
            }
        } catch (Exception e) {
            // JSON parse error
        }
        return songs;
    }

    private List<Song> parsePlaylistResults(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject root = gson.fromJson(json, JsonObject.class);
            JsonArray videos = root.getAsJsonArray("videos");
            if (videos != null) {
                for (int i = 0; i < videos.size(); i++) {
                    JsonObject item = videos.get(i).getAsJsonObject();
                    String id = item.has("videoId") ? item.get("videoId").getAsString() : "";
                    String title = item.has("title") ? item.get("title").getAsString() : "Unknown";
                    String author = item.has("author") ? item.get("author").getAsString() : "Unknown";

                    if (id.isEmpty()) continue;

                    Song song = new Song(id, title, author);
                    song.setSource(Song.AudioSource.YOUTUBE_MUSIC);
                    song.setQuality(Song.AudioQuality.MEDIUM_256);
                    songs.add(song);
                }
            }
        } catch (Exception e) {
            // JSON parse error
        }
        return songs;
    }

    private void resolveAudioStreams(List<Song> songs, SearchResultCallback callback) {
        if (songs.isEmpty()) {
            mainHandler.post(() -> callback.onResults(songs));
            return;
        }

        List<Song> resolved = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(songs.size());

        for (Song song : songs) {
            executor.execute(() -> {
                for (String instance : INV_INSTANCES) {
                    try {
                        String url = instance + "/api/v1/videos/" + song.getId();
                        Request request = new Request.Builder()
                                .url(url)
                                .addHeader("User-Agent", "LossLessMusic/1.0")
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful() && response.body() != null) {
                                String body = response.body().string();
                                JsonObject data = gson.fromJson(body, JsonObject.class);
                                if (data != null && data.has("adaptiveFormats")) {
                                    JsonArray formats = data.getAsJsonArray("adaptiveFormats");
                                    String bestUrl = findBestAudioFormat(formats);
                                    if (bestUrl != null) {
                                        song.setStreamUrl(bestUrl);
                                        synchronized (resolved) {
                                            resolved.add(song);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }

                if (remaining.decrementAndGet() == 0) {
                    mainHandler.post(() -> callback.onResults(resolved));
                }
            });
        }
    }

    private String findBestAudioFormat(JsonArray formats) {
        String bestUrl = null;
        int bestBitrate = 0;

        if (formats == null) return null;

        for (int i = 0; i < formats.size(); i++) {
            JsonObject format = formats.get(i).getAsJsonObject();
            String type = format.has("type") ? format.get("type").getAsString() : "";

            if (!type.contains("audio")) continue;

            int bitrate = 0;
            if (format.has("bitrate")) {
                try {
                    bitrate = Integer.parseInt(format.get("bitrate").getAsString());
                } catch (NumberFormatException ignored) {}
            }

            if (bitrate > bestBitrate) {
                bestBitrate = bitrate;
                if (format.has("url")) {
                    bestUrl = format.get("url").getAsString();
                }
            }
        }

        return bestUrl;
    }
}
