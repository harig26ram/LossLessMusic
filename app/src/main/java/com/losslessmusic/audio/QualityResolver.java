package com.losslessmusic.audio;

import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        for (AudioSourceProvider provider : providers) {
            if (!provider.isAvailable()) continue;
            if (provider.getSourceType() == song.getSource()
                    && song.getStreamUrl() != null) {
                candidates.add(new ResolvedSource(
                        song,
                        provider.getSourceType(),
                        song.getQuality(),
                        song.getStreamUrl()
                ));
            }
        }

        if (candidates.isEmpty()) {
            callback.onResolved(null);
            return;
        }

        Collections.sort(candidates, (a, b) -> {
            int qualityCompare = Integer.compare(
                    b.quality.getRank(), a.quality.getRank());
            if (qualityCompare != 0) return qualityCompare;
            return Boolean.compare(
                    !isPremiumProvider(a.provider),
                    !isPremiumProvider(b.provider));
        });

        callback.onResolved(candidates.get(0));
    }

    public void resolveBestSources(List<Song> songs, MultiResolutionCallback callback) {
        List<ResolvedSource> results = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(songs.size());

        if (remaining.get() == 0) {
            callback.onAllResolved(results);
            return;
        }

        for (Song song : songs) {
            resolveBestSource(song, resolved -> {
                if (resolved != null) {
                    synchronized (results) {
                        results.add(resolved);
                    }
                }
                if (remaining.decrementAndGet() == 0) {
                    callback.onAllResolved(results);
                }
            });
        }
    }

    private boolean isPremiumProvider(Song.AudioSource source) {
        switch (source) {
            case SPOTIFY_PREVIEW:
            case JIOSAAVN:
            case GAANA:
                return true;
            default:
                return false;
        }
    }

    public interface ResolutionCallback {
        void onResolved(ResolvedSource source);
    }

    public interface MultiResolutionCallback {
        void onAllResolved(List<ResolvedSource> sources);
    }
}
