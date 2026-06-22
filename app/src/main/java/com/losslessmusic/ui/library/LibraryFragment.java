package com.losslessmusic.ui.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.losslessmusic.adapters.SongAdapter;
import com.losslessmusic.audio.AudioSourceProvider;
import com.losslessmusic.audio.LocalFileProvider;
import com.losslessmusic.databinding.FragmentLibraryBinding;
import com.losslessmusic.models.Song;
import com.losslessmusic.ui.player.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private SongAdapter adapter;
    private LocalFileProvider localProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localProvider = new LocalFileProvider(requireContext());
        adapter = new SongAdapter();
        binding.libraryList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.libraryList.setAdapter(adapter);

        adapter.setOnSongClickListener(new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song, int position) {
                playSong(song);
            }

            @Override
            public void onSongLongClick(Song song, int position) {
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadLocalFiles);
        binding.swipeRefresh.setColorSchemeColors(
                getResources().getColor(com.google.android.material.R.color.design_default_color_primary, null));

        loadLocalFiles();
    }

    private void loadLocalFiles() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        localProvider.search(null, new AudioSourceProvider.SearchResultCallback() {
            @Override
            public void onResults(List<Song> songs) {
                if (binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (songs.isEmpty()) {
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyState.setVisibility(View.GONE);
                        adapter.submitList(songs);
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void playSong(Song song) {
        ArrayList<Song> singleList = new ArrayList<>();
        singleList.add(song);

        Intent serviceIntent = new Intent(requireContext(),
                com.losslessmusic.audio.PlaybackService.class);
        serviceIntent.setAction("com.losslessmusic.PLAY");
        serviceIntent.putParcelableArrayListExtra("songs", singleList);
        serviceIntent.putExtra("startIndex", 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }

        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
