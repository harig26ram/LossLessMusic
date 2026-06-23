package com.losslessmusic.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.losslessmusic.adapters.SongAdapter;
import com.losslessmusic.audio.ITunesProvider;
import com.losslessmusic.audio.JioSaavnProvider;
import com.losslessmusic.databinding.FragmentHomeBinding;
import com.losslessmusic.models.Song;
import com.losslessmusic.ui.player.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SongAdapter adapter;
    private ITunesProvider iTunesProvider;
    private JioSaavnProvider jiosaavnProvider;

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

        iTunesProvider = new ITunesProvider();
        jiosaavnProvider = new JioSaavnProvider();

        adapter = new SongAdapter();
        binding.songList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.songList.setAdapter(adapter);

        adapter.setOnSongClickListener(new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song, int position) {
                playSong(song, adapter.getCurrentList());
            }

            @Override
            public void onSongLongClick(Song song, int position) {}
        });

        binding.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = v.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                    return true;
                }
                return false;
            }
        });

        android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        binding.searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                if (s.length() >= 2) {
                    searchHandler.postDelayed(() -> {
                        if (isAdded()) performSearch(s.toString());
                    }, 600);
                } else if (s.length() == 0) {
                    binding.emptyState.setVisibility(View.VISIBLE);
                    adapter.submitList(new ArrayList<>());
                }
            }
        });

        showEmptyState();
    }

    private void showEmptyState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
        adapter.submitList(new ArrayList<>());
    }

    private void performSearch(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        List<Song> allResults = new ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(2);

        Runnable onDone = () -> {
            if (remaining.decrementAndGet() == 0 && isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                if (allResults.isEmpty()) {
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
                adapter.submitList(new ArrayList<>(allResults));
            }
        };

        iTunesProvider.search(query, new ITunesProvider.SearchResultCallback() {
            @Override
            public void onResults(List<Song> songs) {
                allResults.addAll(songs);
                onDone.run();
            }

            @Override
            public void onError(String message) {
                onDone.run();
            }
        });

        jiosaavnProvider.search(query, new JioSaavnProvider.SearchResultCallback() {
            @Override
            public void onResults(List<Song> songs) {
                allResults.addAll(songs);
                onDone.run();
            }

            @Override
            public void onError(String message) {
                onDone.run();
            }
        });
    }

    private void playSong(Song song, List<Song> queue) {
        if (song.getStreamUrl() == null) {
            android.widget.Toast.makeText(requireContext(),
                    "No playable source for: " + song.getTitle(),
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putParcelableArrayListExtra("songs", new ArrayList<>(queue));
        intent.putExtra("startIndex", queue.indexOf(song));
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
