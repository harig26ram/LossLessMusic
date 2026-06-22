package com.losslessmusic.audio;

import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QualityResolver {

    public static class ResolvedSource {
        public final Song song;
        public final Song.AudioSource provider;
        public final Song.AudioQuality quality;
        public final String streamUrl;

        public ResolvedSource(Song song, Song.AudioSource provider,
                              Song.AudioQuality quality, String streamUrl) {
            this.song = song;
            this.provider = provider;
            this.quality = quality;
            this.streamUrl = streamUrl;
        }
    }

    private final List<AudioSourceProvider> providers;

    public QualityResolver() {
        providers = new ArrayList<>();
    }

    public void registerProvider(AudioSourceProvider provider) {
        providers.add(provider);
    }

    public void removeProvider(AudioSourceProvider provider) {
        providers.remove(provider);
    }

    public List<AudioSourceProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    public void resolveBestSource(Song song, ResolutionCallback callback) {
        List<ResolvedSource> candidates = new ArrayList<>();

        // Check if song already has a stream URL
        if (song.getStreamUrl() != null) {
            candidates.add(new ResolvedSource(
                    song,
                    song.getSource(),
                    song.getQuality(),
                    song.getStreamUrl()
            ));
            sortAndReturn(candidates, callback);
            return;
        }

        // Check if song has a local URI
        if (song.getLocalUri() != null) {
            candidates.add(new ResolvedSource(
                    song,
                    Song.AudioSource.LOCAL,
                    Song.AudioQuality.LOSSLESS_24,
                    song.getLocalUri().toString()
            ));
            sortAndReturn(candidates, callback);
            return;
        }

        // No stream URL available
        callback.onResolved(null);
    }

    private void sortAndReturn(List<ResolvedSource> candidates, ResolutionCallback callback) {
        if (candidates.isEmpty()) {
            callback.onResolved(null);
            return;
        }

        Collections.sort(candidates, (a, b) -> {
            return Integer.compare(b.quality.getRank(), a.quality.getRank());
        });

        callback.onResolved(candidates.get(0));
    }

    public interface ResolutionCallback {
        void onResolved(ResolvedSource source);
    }
}
