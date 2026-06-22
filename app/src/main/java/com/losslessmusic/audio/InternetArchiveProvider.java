package com.losslessmusic.audio;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.losslessmusic.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InternetArchiveProvider implements AudioSourceProvider {

    private static final String BASE_URL = "https://archive.org/advancedsearch.php";
    private static final String METADATA_URL = "https://archive.org/metadata/";

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public InternetArchiveProvider() {
        client = new OkHttpClient.Builder().build();
        gson = new Gson();
        executor = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public String getProviderName() { return "Internet Archive"; }

    @Override
    public Song.AudioSource getSourceType() { return Song.AudioSource.INTERNET_ARCHIVE; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public boolean isPremiumRequired() { return false; }

    @Override
    public Song.AudioQuality getMaxQuality() { return Song.AudioQuality.LOSSLESS_24; }

    @Override
    public void search(String query, SearchResultCallback callback) {
        executor.execute(() -> {
            try {
                String url = BASE_URL +
                        "?q=" + query.replace(" ", "+") +
                        "&fl[]=identifier,title,creator,description" +
                        "&rows=30&output=json" +
                        "&mediatype=audio";

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseSearchResults(body);
                        mainHandler.post(() -> callback.onResults(songs));
                    } else {
                        mainHandler.post(() -> callback.onError("Search failed"));
                    }
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    @Override
    public void getAlbumTracks(String albumId, SearchResultCallback callback) {
        executor.execute(() -> {
            try {
                String url = METADATA_URL + albumId;
                Request request = new Request.Builder().url(url).build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseMetadataResults(body, albumId);
                        mainHandler.post(() -> callback.onResults(songs));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to load album"));
                    }
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    @Override
    public void getTrending(TrendingCallback callback) {
        executor.execute(() -> {
            try {
                String url = BASE_URL +
                        "?q=mediatype:audio+AND+subject:music" +
                        "&fl[]=identifier,title,creator" +
                        "&sort[]=downloads+desc&rows=30&output=json";

                Request request = new Request.Builder().url(url).build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseSearchResults(body);
                        mainHandler.post(() -> callback.onResults(songs, "Trending Music"));
                    } else {
                        mainHandler.post(() -> callback.onError("Failed to load trending"));
                    }
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private List<Song> parseSearchResults(String json) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            JsonArray docs = response.getAsJsonArray("response.docs");

            if (docs != null) {
                for (int i = 0; i < docs.size(); i++) {
                    JsonObject doc = docs.get(i).getAsJsonObject();
                    String identifier = doc.get("identifier").getAsString();
                    String title = doc.has("title") ?
                            doc.get("title").getAsString() : "Unknown";
                    String creator = doc.has("creator") ?
                            doc.get("creator").getAsString() : "Unknown";

                    Song song = new Song(identifier, title, creator);
                    song.setSource(Song.AudioSource.INTERNET_ARCHIVE);
                    song.setQuality(Song.AudioQuality.LOSSLESS_16);
                    songs.add(song);
                }
            }
        } catch (Exception ignored) {}
        return songs;
    }

    private List<Song> parseMetadataResults(String json, String identifier) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject metadata = gson.fromJson(json, JsonObject.class);
            JsonArray files = metadata.getAsJsonArray("files");

            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    JsonObject file = files.get(i).getAsJsonObject();
                    String name = file.has("name") ?
                            file.get("name").getAsString() : "";

                    if (name.endsWith(".flac") || name.endsWith(".mp3")
                            || name.endsWith(".ogg") || name.endsWith(".wav")) {
                        String title = metadata.has("metadata") ?
                                metadata.getAsJsonObject("metadata")
                                        .get("title").getAsString() : name;
                        String creator = metadata.has("metadata") &&
                                metadata.getAsJsonObject("metadata").has("creator") ?
                                metadata.getAsJsonObject("metadata")
                                        .get("creator").getAsString() : "Unknown";

                        Song song = new Song(identifier + "/" + name, title, creator);
                        song.setSource(Song.AudioSource.INTERNET_ARCHIVE);
                        song.setStreamUrl("https://archive.org/download/" +
                                identifier + "/" + name);

                        if (name.endsWith(".flac")) {
                            song.setQuality(Song.AudioQuality.LOSSLESS_16);
                        } else if (name.endsWith(".wav")) {
                            song.setQuality(Song.AudioQuality.LOSSLESS_24);
                        } else {
                            song.setQuality(Song.AudioQuality.HIGH_320);
                        }
                        songs.add(song);
                    }
                }
            }
        } catch (Exception ignored) {}
        return songs;
    }
}
