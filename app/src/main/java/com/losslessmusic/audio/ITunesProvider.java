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

public class ITunesProvider implements AudioSourceProvider {

    private static final String SEARCH_URL = "https://itunes.apple.com/search";

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public ITunesProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public String getProviderName() { return "iTunes"; }

    @Override
    public Song.AudioSource getSourceType() { return Song.AudioSource.ITUNES; }

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
                String url = SEARCH_URL + "?term=" + encoded
                        + "&media=music&limit=20&country=IN";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.4")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseSearchResults(body);
                        mainHandler.post(() -> callback.onResults(songs));
                    } else {
                        mainHandler.post(() -> callback.onError("Search failed: " + response.code()));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("iTunes: " + e.getMessage()));
            }
        });
    }

    @Override
    public void getAlbumTracks(String albumId, SearchResultCallback callback) {
        mainHandler.post(() -> callback.onError("Not implemented"));
    }

    @Override
    public void getTrending(TrendingCallback callback) {
        executor.execute(() -> {
            try {
                String url = "https://itunes.apple.com/in/rss/topsongs/limit=30/json";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.4")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseTrendingResults(body);
                        mainHandler.post(() -> callback.onResults(songs, "iTunes Top Songs"));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to load trending"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private List<Song> parseSearchResults(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            if (response == null) return songs;

            JsonArray results = response.getAsJsonArray("results");
            if (results == null) return songs;

            for (int i = 0; i < results.size(); i++) {
                try {
                    JsonObject item = results.get(i).getAsJsonObject();
                    Song song = parseTrackJson(item);
                    if (song != null) songs.add(song);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return songs;
    }

    private List<Song> parseTrendingResults(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            if (response == null) return songs;

            JsonObject feed = response.getAsJsonObject("feed");
            if (feed == null) return songs;

            JsonArray entries = feed.getAsJsonArray("entry");
            if (entries == null) return songs;

            for (int i = 0; i < entries.size(); i++) {
                try {
                    JsonObject item = entries.get(i).getAsJsonObject();
                    Song song = parseTrendingEntry(item);
                    if (song != null) songs.add(song);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return songs;
    }

    private Song parseTrackJson(JsonObject item) {
        try {
            String trackId = getJsonString(item, "trackId");
            if (trackId.isEmpty()) return null;

            String title = getJsonString(item, "trackName");
            if (title.isEmpty()) title = getJsonString(item, "collectionName");
            if (title.isEmpty()) return null;

            String artist = getJsonString(item, "artistName");
            if (artist.isEmpty()) artist = "Unknown";

            String previewUrl = getJsonString(item, "previewUrl");
            if (previewUrl.isEmpty()) return null;

            Song song = new Song("itunes_" + trackId, title, artist);
            song.setSource(Song.AudioSource.ITUNES);
            song.setQuality(Song.AudioQuality.HIGH_320);
            song.setStreamUrl(previewUrl);

            String artwork = getJsonString(item, "artworkUrl100");
            if (!artwork.isEmpty()) {
                song.setArtworkUrl(artwork.replace("100x100", "500x500"));
            }

            String album = getJsonString(item, "collectionName");
            if (!album.isEmpty()) song.setAlbum(album);

            return song;
        } catch (Exception e) {
            return null;
        }
    }

    private Song parseTrendingEntry(JsonObject item) {
        try {
            JsonObject idObj = item.has("id") ? item.getAsJsonObject("id") : null;
            String trackId = "";
            if (idObj != null && idObj.has("im:id")) {
                trackId = idObj.get("im:id").getAsString();
            }
            if (trackId.isEmpty()) return null;

            String title = "";
            if (item.has("im:name") && item.getAsJsonObject("im:name").has("label")) {
                title = item.getAsJsonObject("im:name").get("label").getAsString();
            }
            if (title.isEmpty()) return null;

            String artist = "";
            if (item.has("im:artist") && item.getAsJsonObject("im:artist").has("label")) {
                artist = item.getAsJsonObject("im:artist").get("label").getAsString();
            }

            String artwork = "";
            JsonArray images = item.getAsJsonArray("im:image");
            if (images != null && images.size() > 0) {
                JsonObject lastImg = images.get(images.size() - 1).getAsJsonObject();
                if (lastImg.has("label")) {
                    artwork = lastImg.get("label").getAsString();
                }
            }

            String previewUrl = "";
            JsonArray links = item.getAsJsonArray("link");
            if (links != null) {
                for (int i = 0; i < links.size(); i++) {
                    JsonObject link = links.get(i).getAsJsonObject();
                    if (link.has("attributes")) {
                        JsonObject attrs = link.getAsJsonObject("attributes");
                        if ("audio".equals(getJsonString(attrs, "type"))) {
                            previewUrl = getJsonString(attrs, "href");
                            break;
                        }
                    }
                }
            }

            if (previewUrl.isEmpty()) return null;

            Song song = new Song("itunes_" + trackId, title, artist);
            song.setSource(Song.AudioSource.ITUNES);
            song.setQuality(Song.AudioQuality.HIGH_320);
            song.setStreamUrl(previewUrl);
            if (!artwork.isEmpty()) song.setArtworkUrl(artwork);

            return song;
        } catch (Exception e) {
            return null;
        }
    }

    private String getJsonString(JsonObject obj, String key) {
        try {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        } catch (Exception ignored) {}
        return "";
    }
}
