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
import com.losslessmusic.audio.ITunesProvider;
import com.losslessmusic.audio.JioSaavnProvider;
import com.losslessmusic.audio.LocalFileProvider;
import com.losslessmusic.audio.RadioBrowserProvider;
import com.losslessmusic.databinding.FragmentHomeBinding;
import com.losslessmusic.models.Song;
import com.losslessmusic.ui.player.PlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SongAdapter trendingAdapter;
    private SongAdapter searchAdapter;
    private LocalFileProvider localProvider;
    private InternetArchiveProvider archiveProvider;
    private JioSaavnProvider jiosaavnProvider;
    private ITunesProvider iTunesProvider;
    private RadioBrowserProvider radioProvider;

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
        jiosaavnProvider = new JioSaavnProvider();
        iTunesProvider = new ITunesProvider();
        radioProvider = new RadioBrowserProvider();

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
                playSong(song, trendingAdapter.getCurrentList());
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
                playSong(song, searchAdapter.getCurrentList());
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
                    binding.trendingSection.setVisibility(View.VISIBLE);
                    binding.searchResultsSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        binding.searchBar.setOnClickListener(v -> {
            boolean showing = binding.searchResultsSection.getVisibility() == View.VISIBLE;
            if (showing) {
                binding.trendingSection.setVisibility(View.VISIBLE);
                binding.searchResultsSection.setVisibility(View.GONE);
            } else {
                binding.trendingSection.setVisibility(View.GONE);
                binding.searchResultsSection.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadTrending() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        List<Song> allSongs = new ArrayList<>();
        AtomicInteger sourcesRemaining = new AtomicInteger(4);

        AudioSourceProvider.TrendingCallback aggregateCallback = new AudioSourceProvider.TrendingCallback() {
            @Override
            public synchronized void onResults(List<Song> songs, String category) {
                allSongs.addAll(songs);
                if (sourcesRemaining.decrementAndGet() == 0 && binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (allSongs.isEmpty()) {
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyState.setVisibility(View.GONE);
                        trendingAdapter.submitList(new ArrayList<>(allSongs));
                    }
                }
            }

            @Override
            public synchronized void onError(String message) {
                if (sourcesRemaining.decrementAndGet() == 0 && binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (allSongs.isEmpty()) {
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyState.setVisibility(View.GONE);
                        trendingAdapter.submitList(new ArrayList<>(allSongs));
                    }
                }
            }
        };

        try {
            localProvider.getTrending(new AudioSourceProvider.TrendingCallback() {
                @Override
                public void onResults(List<Song> songs, String category) {
                    aggregateCallback.onResults(songs, category);
                }
                @Override
                public void onError(String message) {
                    aggregateCallback.onError(message);
                }
            });
        } catch (Exception e) {
            aggregateCallback.onError("local");
        }

        archiveProvider.getTrending(new AudioSourceProvider.TrendingCallback() {
            @Override
            public void onResults(List<Song> songs, String category) {
                aggregateCallback.onResults(songs, category);
            }
            @Override
            public void onError(String message) {
                aggregateCallback.onError(message);
            }
        });

        jiosaavnProvider.getTrending(new AudioSourceProvider.TrendingCallback() {
            @Override
            public void onResults(List<Song> songs, String category) {
                aggregateCallback.onResults(songs, category);
            }
            @Override
            public void onError(String message) {
                aggregateCallback.onError(message);
            }
        });

        radioProvider.getTrending(new AudioSourceProvider.TrendingCallback() {
            @Override
            public void onResults(List<Song> songs, String category) {
                aggregateCallback.onResults(songs, category);
            }
            @Override
            public void onError(String message) {
                aggregateCallback.onError(message);
            }
        });
    }

    private void performSearch(String query) {
        binding.searchProgress.setVisibility(View.VISIBLE);

        List<Song> allResults = new ArrayList<>();
        AtomicInteger sourcesRemaining = new AtomicInteger(3);

        AudioSourceProvider.SearchResultCallback aggregateCallback = new AudioSourceProvider.SearchResultCallback() {
            @Override
            public synchronized void onResults(List<Song> songs) {
                allResults.addAll(songs);
                if (sourcesRemaining.decrementAndGet() == 0 && binding != null) {
                    binding.searchProgress.setVisibility(View.GONE);
                    if (allResults.isEmpty()) {
                        Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                    searchAdapter.submitList(allResults);
                }
            }

            @Override
            public synchronized void onError(String message) {
                if (sourcesRemaining.decrementAndGet() == 0 && binding != null) {
                    binding.searchProgress.setVisibility(View.GONE);
                    if (allResults.isEmpty()) {
                        Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show();
                    } else {
                        searchAdapter.submitList(allResults);
                    }
                }
            }
        };

        archiveProvider.search(query, new AudioSourceProvider.SearchResultCallback() {
            @Override
            public void onResults(List<Song> songs) { aggregateCallback.onResults(songs); }
            @Override
            public void onError(String message) { aggregateCallback.onError(message); }
        });

        jiosaavnProvider.search(query, new AudioSourceProvider.SearchResultCallback() {
            @Override
            public void onResults(List<Song> songs) { aggregateCallback.onResults(songs); }
            @Override
            public void onError(String message) { aggregateCallback.onError(message); }
        });

        iTunesProvider.search(query, new AudioSourceProvider.SearchResultCallback() {
            @Override
            public void onResults(List<Song> songs) { aggregateCallback.onResults(songs); }
            @Override
            public void onError(String message) { aggregateCallback.onError(message); }
        });
    }

    private void playSong(Song song, List<Song> queue) {
        if (song.getStreamUrl() == null && song.getLocalUri() == null) {
            Toast.makeText(requireContext(), "No playable source for: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            return;
        }

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
        serviceIntent.putParcelableArrayListExtra("songs", songList);
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
