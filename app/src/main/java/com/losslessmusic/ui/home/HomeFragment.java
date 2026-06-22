package com.losslessmusic.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.losslessmusic.adapters.SongAdapter;
import com.losslessmusic.audio.AudioSourceProvider;
import com.losslessmusic.audio.InternetArchiveProvider;
import com.losslessmusic.audio.LocalFileProvider;
import com.losslessmusic.audio.QualityResolver;
import com.losslessmusic.databinding.FragmentHomeBinding;
import com.losslessmusic.models.Song;
import com.losslessmusic.ui.player.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SongAdapter trendingAdapter;
    private SongAdapter searchAdapter;
    private LocalFileProvider localProvider;
    private InternetArchiveProvider archiveProvider;
    private QualityResolver qualityResolver;
    private boolean showingSearch = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localProvider = new LocalFileProvider(requireContext());
        archiveProvider = new InternetArchiveProvider();

        qualityResolver = new QualityResolver();
        qualityResolver.registerProvider(localProvider);
        qualityResolver.registerProvider(archiveProvider);

        setupTrending();
        setupSearch();
        loadTrending();
    }

    private void setupTrending() {
        trendingAdapter = new SongAdapter();
        binding.trendingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.trendingList.setAdapter(trendingAdapter);

        trendingAdapter.setOnSongClickListener(new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song, int position) {
                playSongWithBestQuality(song, trendingAdapter.getCurrentList());
            }

            @Override
            public void onSongLongClick(Song song, int position) {}
        });
    }

    private void setupSearch() {
        searchAdapter = new SongAdapter();
        binding.searchResultsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.searchResultsList.setAdapter(searchAdapter);

        searchAdapter.setOnSongClickListener(new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song, int position) {
                playSongWithBestQuality(song, searchAdapter.getCurrentList());
            }

            @Override
            public void onSongLongClick(Song song, int position) {}
        });

        binding.searchView.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    performSearch(s.toString());
                } else if (s.length() == 0) {
                    showingSearch = false;
                    binding.trendingSection.setVisibility(View.VISIBLE);
                    binding.searchResultsSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        binding.searchBar.setOnClickListener(v -> {
            showingSearch = !showingSearch;
            if (showingSearch) {
                binding.trendingSection.setVisibility(View.GONE);
                binding.searchResultsSection.setVisibility(View.VISIBLE);
            } else {
                binding.trendingSection.setVisibility(View.VISIBLE);
                binding.searchResultsSection.setVisibility(View.GONE);
            }
        });
    }

    private void loadTrending() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        List<Song> allSongs = new ArrayList<>();

        // First load local files (fast, synchronous)
        localProvider.getTrending(new AudioSourceProvider.TrendingCallback() {
            @Override
            public void onResults(List<Song> songs, String category) {
                allSongs.addAll(songs);
                if (binding != null) {
                    trendingAdapter.submitList(new ArrayList<>(allSongs));
                    binding.progressBar.setVisibility(View.GONE);
                }
                // Then load from Internet Archive (async, slower)
                loadArchiveTrending(allSongs);
            }

            @Override
            public void onError(String message) {
                loadArchiveTrending(allSongs);
            }
        });
    }

    private void loadArchiveTrending(List<Song> existing) {
        archiveProvider.getTrending(new AudioSourceProvider.TrendingCallback() {
            @Override
            public void onResults(List<Song> songs, String category) {
                if (binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (songs.isEmpty() && existing.isEmpty()) {
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyState.setVisibility(View.GONE);
                        existing.addAll(songs);
                        trendingAdapter.submitList(new ArrayList<>(existing));
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (existing.isEmpty()) {
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        trendingAdapter.submitList(new ArrayList<>(existing));
                    }
                }
            }
        });
    }

    private void performSearch(String query) {
        binding.searchProgress.setVisibility(View.VISIBLE);
        List<Song> results = new ArrayList<>();

        archiveProvider.search(query, new AudioSourceProvider.SearchResultCallback() {
            @Override
            public void onResults(List<Song> songs) {
                results.addAll(songs);
                localProvider.search(query, new AudioSourceProvider.SearchResultCallback() {
                    @Override
                    public void onResults(List<Song> localSongs) {
                        results.addAll(0, localSongs);
                        if (binding != null) {
                            binding.searchProgress.setVisibility(View.GONE);
                            if (results.isEmpty()) {
                                Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
                            }
                            searchAdapter.submitList(results);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (binding != null) {
                            binding.searchProgress.setVisibility(View.GONE);
                            searchAdapter.submitList(results);
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                localProvider.search(query, new AudioSourceProvider.SearchResultCallback() {
                    @Override
                    public void onResults(List<Song> localSongs) {
                        if (binding != null) {
                            binding.searchProgress.setVisibility(View.GONE);
                            searchAdapter.submitList(localSongs);
                        }
                    }

                    @Override
                    public void onError(String message2) {
                        if (binding != null) {
                            binding.searchProgress.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void playSongWithBestQuality(Song song, List<Song> queue) {
        if (song.getStreamUrl() != null || song.getLocalUri() != null) {
            playSong(song, queue);
            return;
        }

        Toast.makeText(requireContext(), "Resolving best quality...", Toast.LENGTH_SHORT).show();

        qualityResolver.resolveBestSource(song, resolved -> {
            if (resolved != null) {
                song.setStreamUrl(resolved.streamUrl);
                song.setSource(resolved.provider);
                song.setQuality(resolved.quality);
                playSong(song, queue);
            } else {
                Toast.makeText(requireContext(),
                        "No playable source found for: " + song.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playSong(Song song, List<Song> queue) {
        Intent serviceIntent = new Intent(requireContext(),
                com.losslessmusic.audio.PlaybackService.class);
        serviceIntent.setAction("com.losslessmusic.PLAY");

        int startIndex = 0;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(song.getId())) {
                startIndex = i;
                break;
            }
        }

        ArrayList<Song> songList = new ArrayList<>(queue);
        serviceIntent.putExtra("songs", songList);
        serviceIntent.putExtra("startIndex", startIndex);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }

        Intent playerIntent = new Intent(requireContext(), PlayerActivity.class);
        startActivity(playerIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
