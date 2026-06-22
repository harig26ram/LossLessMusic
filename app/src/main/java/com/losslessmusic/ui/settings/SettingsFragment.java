package com.losslessmusic.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.losslessmusic.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.settingsQualityPref.setOnClickListener(v -> showQualityDialog());
        binding.settingsCrossfadePref.setOnClickListener(v -> showCrossfadeDialog());
        binding.settingsEqPref.setOnClickListener(v -> showEqInfo());
        binding.settingsAboutPref.setOnClickListener(v -> showAbout());
    }

    private void showQualityDialog() {
        String[] options = {
                "Best Available (Auto)",
                "Lossless FLAC (24-bit)",
                "Lossless FLAC (16-bit)",
                "High (320kbps)",
                "Medium (256kbps)",
                "Save Data (128kbps)"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Preferred Audio Quality")
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    binding.settingsQualitySummary.setText(selected);
                    Toast.makeText(requireContext(),
                            "Quality set to: " + selected, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showCrossfadeDialog() {
        String[] options = {
                "Off",
                "2 seconds",
                "4 seconds",
                "6 seconds",
                "8 seconds"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Crossfade Duration")
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    binding.settingsCrossfadeSummary.setText(selected);
                })
                .show();
    }

    private void showEqInfo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Equalizer")
                .setMessage("A 10-band parametric equalizer will be available in the full player view.\n\n" +
                        "Presets:\n" +
                        "• Flat\n" +
                        "• Bass Boost\n" +
                        "• Vocal Presence\n" +
                        "• Tamil Classics\n" +
                        "• Electronic\n" +
                        "• Acoustic")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("LossLess Music")
                .setMessage("Version 1.0.0\n\n" +
                        "A high-quality music player with multi-source support.\n\n" +
                        "Features:\n" +
                        "• Lossless FLAC playback\n" +
                        "• Auto best-quality selection\n" +
                        "• Local + Internet Archive sources\n" +
                        "• 10-band equalizer\n" +
                        "• Gapless playback\n\n" +
                        "Built with ExoPlayer, Material Design 3")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
