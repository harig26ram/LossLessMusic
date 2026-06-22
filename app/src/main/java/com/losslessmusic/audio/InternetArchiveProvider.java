package com.losslessmusic.audio;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.losslessmusic.models.Song;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InternetArchiveProvider implements AudioSourceProvider {

    private static final String SEARCH_URL = "https://archive.org/advancedsearch.php";
    private static final String METADATA_URL = "https://archive.org/metadata/";
    private static final String DOWNLOAD_URL = "https://archive.org/download/";

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public InternetArchiveProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();
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
                String encoded = URLEncoder.encode(query, "UTF-8");
                String url = SEARCH_URL +
                        "?q=" + encoded +
                        "&fl[]=identifier,title,creator,item_size" +
                        "&rows=20&output=json" +
                        "&mediatype=audio" +
                        "&sort[]=downloads+desc";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.0")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseSearchResults(body);
                        resolveStreamUrls(songs, callback);
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
                String url = METADATA_URL + albumId;
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.0")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseMetadataFiles(body, albumId);
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
                String url = SEARCH_URL +
                        "?q=mediatype%3Aaudio+AND+subject%3Amusic" +
                        "&fl[]=identifier,title,creator,item_size" +
                        "&sort[]=downloads+desc&rows=30&output=json";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.0")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> songs = parseSearchResults(body);
                        resolveStreamUrlsForTrending(songs, callback);
                    } else {
                        mainHandler.post(() -> callback.onError("Failed: " + response.code()));
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
            JsonArray docs = response.getAsJsonObject("response").getAsJsonArray("docs");

            if (docs != null) {
                for (int i = 0; i < docs.size(); i++) {
                    JsonObject doc = docs.get(i).getAsJsonObject();
                    String identifier = doc.get("identifier").getAsString();
                    String title = doc.has("title") ? doc.get("title").getAsString() : "Unknown";
                    String creator = doc.has("creator") ? doc.get("creator").getAsString() : "Unknown";

                    Song song = new Song(identifier, title, creator);
                    song.setSource(Song.AudioSource.INTERNET_ARCHIVE);
                    song.setQuality(Song.AudioQuality.UNKNOWN);
                    songs.add(song);
                }
            }
        } catch (Exception e) {
            // JSON parse error
        }
        return songs;
    }

    private void resolveStreamUrls(List<Song> songs, SearchResultCallback callback) {
        if (songs.isEmpty()) {
            mainHandler.post(() -> callback.onResults(songs));
            return;
        }

        List<Song> resolved = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(songs.size());

        for (Song song : songs) {
            resolveSingleItem(song, resolved, remaining, callback);
        }
    }

    private void resolveStreamUrlsForTrending(List<Song> songs, TrendingCallback callback) {
        if (songs.isEmpty()) {
            mainHandler.post(() -> callback.onResults(songs, "Trending Music"));
            return;
        }

        List<Song> resolved = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(songs.size());

        for (Song song : songs) {
            executor.execute(() -> {
                try {
                    String url = METADATA_URL + song.getId();
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", "LossLessMusic/1.0")
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String body = response.body().string();
                            List<Song> tracks = parseMetadataFiles(body, song.getId());
                            if (!tracks.isEmpty()) {
                                Song best = pickBestTrack(tracks);
                                synchronized (resolved) {
                                    resolved.add(best);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}

                if (remaining.decrementAndGet() == 0) {
                    mainHandler.post(() -> callback.onResults(resolved, "Trending Music"));
                }
            });
        }
    }

    private void resolveSingleItem(Song song, List<Song> resolved,
                                    AtomicInteger remaining, SearchResultCallback callback) {
        executor.execute(() -> {
            try {
                String url = METADATA_URL + song.getId();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "LossLessMusic/1.0")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<Song> tracks = parseMetadataFiles(body, song.getId());
                        if (!tracks.isEmpty()) {
                            Song best = pickBestTrack(tracks);
                            synchronized (resolved) {
                                resolved.add(best);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

            if (remaining.decrementAndGet() == 0) {
                mainHandler.post(() -> callback.onResults(resolved));
            }
        });
    }

    private List<Song> parseMetadataFiles(String json, String identifier) {
        List<Song> songs = new ArrayList<>();
        try {
            JsonObject root = gson.fromJson(json, JsonObject.class);
            JsonObject metadata = root.has("metadata") ? root.getAsJsonObject("metadata") : null;
            JsonArray files = root.getAsJsonArray("files");

            String title = metadata != null && metadata.has("title") ?
                    metadata.get("title").getAsString() : identifier;
            String creator = metadata != null && metadata.has("creator") ?
                    metadata.get("creator").getAsString() : "Unknown";

            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    JsonObject file = files.get(i).getAsJsonObject();
                    String name = file.has("name") ? file.get("name").getAsString() : "";
                    String format = file.has("format") ? file.get("format").getAsString() : "";

                    boolean isAudio = name.endsWith(".flac") || name.endsWith(".mp3")
                            || name.endsWith(".ogg") || name.endsWith(".wav")
                            || name.endsWith(".m4a") || name.endsWith(".aac")
                            || format.contains("VBR") || format.contains("MP3")
                            || format.contains(" FLAC") || format.contains("Ogg Vorbis");

                    if (isAudio) {
                        Song song = new Song(identifier + "/" + name, title, creator);
                        song.setSource(Song.AudioSource.INTERNET_ARCHIVE);
                        song.setStreamUrl(DOWNLOAD_URL + identifier + "/" +
                                URLEncoder.encode(name, "UTF-8"));

                        if (name.endsWith(".flac") || format.contains(" FLAC")) {
                            song.setQuality(Song.AudioQuality.LOSSLESS_16);
                        } else if (name.endsWith(".wav")) {
                            song.setQuality(Song.AudioQuality.LOSSLESS_24);
                        } else if (name.endsWith(".mp3") || format.contains("MP3")) {
                            song.setQuality(Song.AudioQuality.HIGH_320);
                        } else {
                            song.setQuality(Song.AudioQuality.MEDIUM_256);
                        }

                        String sizeStr = file.has("size") ? file.get("size").getAsString() : "0";
                        try {
                            song.setDurationMs(Long.parseLong(sizeStr) / 16000 * 1000);
                        } catch (NumberFormatException ignored) {}

                        songs.add(song);
                    }
                }
            }
        } catch (Exception e) {
            // JSON parse error
        }
        return songs;
    }

    private Song pickBestTrack(List<Song> tracks) {
        return tracks.stream()
                .max((a, b) -> Integer.compare(
                        a.getQuality().getRank(), b.getQuality().getRank()))
                .orElse(tracks.get(0));
    }
}
