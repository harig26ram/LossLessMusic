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
        mainHandler.post(() -> callback.onError("Not implemented"));
    }

    @Override
    public void getTrending(TrendingCallback callback) {
        executor.execute(() -> {
            try {
                String url = API_BASE + "?__call=autocomplete.get&_format=json&_marker=0&cc=in&query=trending+tamil";
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
            if (songsData == null) return songs;

            JsonArray data = songsData.getAsJsonArray("data");
            if (data == null) return songs;

            for (int i = 0; i < Math.min(data.size(), 15); i++) {
                try {
                    JsonObject item = data.get(i).getAsJsonObject();
                    Song song = parseSongFromJson(item);
                    if (song != null) songs.add(song);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return songs;
    }

    private Song parseSongFromJson(JsonObject item) {
        try {
            // Verified field names from actual API response:
            // "id", "title", "image", "album", "url", "type",
            // "more_info.vlink", "more_info.primary_artists", "more_info.singers"

            String id = getJsonString(item, "id");
            if (id.isEmpty()) return null;

            // Title is at top level
            String title = getJsonString(item, "title");
            if (title.isEmpty()) title = "Unknown";

            // Artist is in more_info
            String artist = "Unknown";
            JsonObject moreInfo = item.has("more_info") ? item.getAsJsonObject("more_info") : null;
            if (moreInfo != null) {
                String primary = getJsonString(moreInfo, "primary_artists");
                if (!primary.isEmpty()) {
                    artist = primary;
                } else {
                    String singers = getJsonString(moreInfo, "singers");
                    if (!singers.isEmpty()) artist = singers;
                }
            }

            Song song = new Song(id, title, artist);
            song.setSource(Song.AudioSource.JIOSAAVN);
            song.setQuality(Song.AudioQuality.HIGH_320);

            // Artwork from top level
            String image = getJsonString(item, "image");
            if (!image.isEmpty()) {
                // Replace small image with larger one
                song.setArtworkUrl(image.replace("50x50", "500x500"));
            }

            // Preview URL from more_info.vlink
            if (moreInfo != null) {
                String vlink = getJsonString(moreInfo, "vlink");
                if (!vlink.isEmpty()) {
                    song.setStreamUrl(vlink);
                    song.setQuality(Song.AudioQuality.LOW_128);
                }
            }

            // Album
            String album = getJsonString(item, "album");
            if (!album.isEmpty()) song.setAlbum(album);

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
