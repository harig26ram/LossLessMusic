package com.losslessmusic.audio;

import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.Collections;
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

        if (!candidates.isEmpty()) {
            sortAndReturn(candidates, callback);
            return;
        }

        searchAcrossProviders(song, candidates, callback);
    }

    private void searchAcrossProviders(Song song, List<ResolvedSource> candidates,
                                        ResolutionCallback callback) {
        if (providers.isEmpty()) {
            callback.onResolved(null);
            return;
        }

        AtomicInteger remaining = new AtomicInteger(providers.size());

        for (AudioSourceProvider provider : providers) {
            if (!provider.isAvailable()) {
                if (remaining.decrementAndGet() == 0) {
                    sortAndReturn(candidates, callback);
                }
                continue;
            }

            String query = song.getTitle() + " " + song.getArtist();
            provider.search(query, new AudioSourceProvider.SearchResultCallback() {
                @Override
                public void onResults(List<Song> songs) {
                    for (Song found : songs) {
                        if (isMatchingSong(song, found) && found.getStreamUrl() != null) {
                            synchronized (candidates) {
                                candidates.add(new ResolvedSource(
                                        found,
                                        provider.getSourceType(),
                                        found.getQuality(),
                                        found.getStreamUrl()
                                ));
                            }
                            break;
                        }
                    }
                    if (remaining.decrementAndGet() == 0) {
                        sortAndReturn(candidates, callback);
                    }
                }

                @Override
                public void onError(String message) {
                    if (remaining.decrementAndGet() == 0) {
                        sortAndReturn(candidates, callback);
                    }
                }
            });
        }
    }

    private boolean isMatchingSong(Song original, Song found) {
        String origTitle = normalize(original.getTitle());
        String foundTitle = normalize(found.getTitle());
        String origArtist = normalize(original.getArtist());
        String foundArtist = normalize(found.getArtist());

        boolean titleMatch = origTitle.equals(foundTitle)
                || origTitle.contains(foundTitle)
                || foundTitle.contains(origTitle);

        boolean artistMatch = origArtist.equals(foundArtist)
                || origArtist.contains(foundArtist)
                || foundArtist.contains(origArtist);

        return titleMatch && artistMatch;
    }

    private String normalize(String s) {
        return s.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void sortAndReturn(List<ResolvedSource> candidates, ResolutionCallback callback) {
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
