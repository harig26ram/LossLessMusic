package com.losslessmusic.audio;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RadioBrowserProvider implements AudioSourceProvider {

    private static final String API_BASE = "https://de1.api.radio-browser.info/json";

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public RadioBrowserProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public String getProviderName() { return "Radio Browser"; }

    @Override
    public Song.AudioSource getSourceType() { return Song.AudioSource.RADIO_BROWSER; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public boolean isPremiumRequired() { return false; }

    @Override
    public Song.AudioQuality getMaxQuality() { return Song.AudioQuality.MEDIUM_256; }

    @Override
    public void search(String query, SearchResultCallback callback) {
        executor.execute(() -> {
            try {
                String encoded = query.replace(" ", "%20");
                String url = API_BASE + "/stations/search?name=" + encoded
                        + "&limit=20&order=votes";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.4")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseStations(body);
                        mainHandler.post(() -> callback.onResults(songs));
                    } else {
                        mainHandler.post(() -> callback.onError("Search failed: " + response.code()));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Radio Browser: " + e.getMessage()));
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
                String url = API_BASE + "/stations/search?name=tamil&limit=20&order=votes";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.4")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseStations(body);
                        mainHandler.post(() -> callback.onResults(songs, "Tamil Radio"));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to load radio stations"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private List<Song> parseStations(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonArray stations = gson.fromJson(json, JsonArray.class);
            if (stations == null) return songs;

            for (int i = 0; i < stations.size(); i++) {
                try {
                    JsonObject station = stations.get(i).getAsJsonObject();
                    Song song = parseStation(station);
                    if (song != null) songs.add(song);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return songs;
    }

    private Song parseStation(JsonObject station) {
        try {
            String stationId = getJsonString(station, "stationuuid");
            if (stationId.isEmpty()) return null;

            String name = getJsonString(station, "name");
            if (name.isEmpty()) return null;

            String url = getJsonString(station, "url");
            if (url.isEmpty()) return null;

            String codec = getJsonString(station, "codec");
            int bitrate = station.has("bitrate") ? station.get("bitrate").getAsInt() : 0;

            Song song = new Song("radio_" + stationId, name, "Live Radio");
            song.setSource(Song.AudioSource.RADIO_BROWSER);
            song.setStreamUrl(url);

            if (bitrate >= 192 || "FLAC".equals(codec) || "AAC".equals(codec)) {
                song.setQuality(Song.AudioQuality.HIGH_320);
            } else if (bitrate >= 128) {
                song.setQuality(Song.AudioQuality.MEDIUM_256);
            } else {
                song.setQuality(Song.AudioQuality.LOW_128);
            }

            String favicon = getJsonString(station, "favicon");
            if (!favicon.isEmpty()) song.setArtworkUrl(favicon);

            String tags = getJsonString(station, "tags");
            if (!tags.isEmpty()) song.setAlbum(tags);

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
