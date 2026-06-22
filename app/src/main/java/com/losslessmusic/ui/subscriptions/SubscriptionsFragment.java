package com.losslessmusic.ui.subscriptions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.losslessmusic.adapters.ServiceAdapter;
import com.losslessmusic.audio.InternetArchiveProvider;
import com.losslessmusic.audio.JioSaavnProvider;
import com.losslessmusic.audio.YouTubeProvider;
import com.losslessmusic.databinding.FragmentSubscriptionsBinding;
import com.losslessmusic.models.SubscriptionConfig;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionsFragment extends Fragment {

    private FragmentSubscriptionsBinding binding;
    private ServiceAdapter adapter;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "lossless_services";
    private static final String KEY_JIOSAAVN = "jiosaavn_linked";
    private static final String KEY_YOUTUBE = "youtube_linked";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSubscriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        adapter = new ServiceAdapter();
        binding.servicesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.servicesList.setAdapter(adapter);

        adapter.setOnServiceClickListener(new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onServiceClick(SubscriptionConfig service) {
                showServiceDetails(service);
            }

            @Override
            public void onToggleService(SubscriptionConfig service, boolean enable) {
                handleServiceToggle(service, enable);
            }
        });

        loadServices();
    }

    private void loadServices() {
        List<SubscriptionConfig> services = new ArrayList<>();

        services.add(new SubscriptionConfig(SubscriptionConfig.ServiceType.LOCAL));
        services.add(new SubscriptionConfig(SubscriptionConfig.ServiceType.INTERNET_ARCHIVE));

        SubscriptionConfig jiosaavn = new SubscriptionConfig(SubscriptionConfig.ServiceType.JIOSAAVN);
        jiosaavn.setLinked(prefs.getBoolean(KEY_JIOSAAVN, false));
        services.add(jiosaavn);

        SubscriptionConfig youtube = new SubscriptionConfig(SubscriptionConfig.ServiceType.YOUTUBE_MUSIC);
        youtube.setLinked(prefs.getBoolean(KEY_YOUTUBE, false));
        services.add(youtube);

        adapter.setServices(services);
    }

    private void handleServiceToggle(SubscriptionConfig service, boolean enable) {
        switch (service.getType()) {
            case JIOSAAVN:
                if (enable) {
                    linkJioSaavn(service);
                } else {
                    unlinkService(service, KEY_JIOSAAVN);
                }
                break;
            case YOUTUBE_MUSIC:
                if (enable) {
                    linkYouTube(service);
                } else {
                    unlinkService(service, KEY_YOUTUBE);
                }
                break;
            case LOCAL:
            case INTERNET_ARCHIVE:
                Toast.makeText(requireContext(),
                        service.getServiceName() + " is always available",
                        Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                break;
            default:
                Toast.makeText(requireContext(),
                        "Coming soon: " + service.getServiceName(),
                        Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private void linkJioSaavn(SubscriptionConfig service) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Connect JioSaavn")
                .setMessage("JioSaavn provides free access to 80M+ songs in high quality (up to 320kbps AAC).\n\n" +
                        "No login required - the app uses JioSaavn's public API.\n\n" +
                        "Features:\n" +
                        "- Search all JioSaavn catalog\n" +
                        "- Stream up to 320kbps\n" +
                        "- Access to playlists and albums\n" +
                        "- Tamil, Hindi, English & regional languages")
                .setPositiveButton("Connect", (dialog, which) -> {
                    service.setLinked(true);
                    prefs.edit().putBoolean(KEY_JIOSAAVN, true).apply();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(),
                            "JioSaavn connected! You can now search and play from JioSaavn.",
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyDataSetChanged();
                })
                .setOnCancelListener(dialog -> adapter.notifyDataSetChanged())
                .show();
    }

    private void linkYouTube(SubscriptionConfig service) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Connect YouTube Music")
                .setMessage("YouTube Music provides access to millions of songs via public Invidious API.\n\n" +
                        "No login required - uses free Invidious instances.\n\n" +
                        "Features:\n" +
                        "- Search YouTube Music catalog\n" +
                        "- Stream up to 256kbps AAC\n" +
                        "- Access to music videos and audio\n" +
                        "- Trending music in your region")
                .setPositiveButton("Connect", (dialog, which) -> {
                    service.setLinked(true);
                    prefs.edit().putBoolean(KEY_YOUTUBE, true).apply();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(),
                            "YouTube Music connected! Search and play from YouTube.",
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyDataSetChanged();
                })
                .setOnCancelListener(dialog -> adapter.notifyDataSetChanged())
                .show();
    }

    private void unlinkService(SubscriptionConfig service, String prefKey) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Disconnect " + service.getServiceName())
                .setMessage("Are you sure you want to disconnect " + service.getServiceName() + "?\n\n" +
                        "You won't be able to search or play from this service.")
                .setPositiveButton("Disconnect", (dialog, which) -> {
                    service.setLinked(false);
                    prefs.edit().putBoolean(prefKey, false).apply();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(),
                            service.getServiceName() + " disconnected",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyDataSetChanged();
                })
                .setOnCancelListener(dialog -> adapter.notifyDataSetChanged())
                .show();
    }

    private void showServiceDetails(SubscriptionConfig service) {
        if (service.getType().isAlwaysAvailable()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(service.getServiceName())
                    .setMessage(getServiceDescription(service.getType()))
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        binding.serviceDetailCard.setVisibility(View.VISIBLE);
        binding.detailServiceName.setText(service.getServiceName());

        if (service.isLinked()) {
            binding.detailServiceStatus.setText("Connected");
            binding.detailServiceStatus.setTextColor(
                    requireContext().getColor(com.losslessmusic.R.color.color_primary));
            binding.linkServiceButton.setText("Disconnect");
            binding.linkServiceButton.setOnClickListener(v -> {
                handleServiceToggle(service, false);
                binding.serviceDetailCard.setVisibility(View.GONE);
            });
        } else {
            binding.detailServiceStatus.setText("Not linked");
            binding.detailServiceStatus.setTextColor(
                    requireContext().getColor(com.losslessmusic.R.color.text_hint));
            binding.linkServiceButton.setText("Connect");
            binding.linkServiceButton.setOnClickListener(v -> {
                handleServiceToggle(service, true);
                binding.serviceDetailCard.setVisibility(View.GONE);
            });
        }

        binding.detailQualityLabel.setText(getQualityDescription(service.getType()));
    }

    private String getServiceDescription(SubscriptionConfig.ServiceType type) {
        switch (type) {
            case LOCAL:
                return "Access music files stored on your device.\n\n" +
                        "Supported formats:\n" +
                        "- FLAC (up to 24-bit/192kHz)\n" +
                        "- WAV, AAC, MP3, OGG\n" +
                        "- ALAC (Apple Lossless)\n\n" +
                        "Quality: Best available (depends on file)";
            case INTERNET_ARCHIVE:
                return "Free access to millions of audio files on Internet Archive.\n\n" +
                        "Features:\n" +
                        "- Public domain music\n" +
                        "- Live concerts and recordings\n" +
                        "- FLAC and MP3 formats\n" +
                        "- No account required\n\n" +
                        "Quality: Up to FLAC 16-bit";
            default:
                return "";
        }
    }

    private String getQualityDescription(SubscriptionConfig.ServiceType type) {
        switch (type) {
            case JIOSAAVN:
                return "Up to 320kbps AAC\nFree tier includes high quality streaming";
            case GAANA:
                return "Up to 320kbps AAC\nFree tier available";
            case SPOTIFY:
                return "Up to 320kbps Ogg Vorbis\nPremium required for highest quality";
            case YOUTUBE_MUSIC:
                return "Up to 256kbps AAC\nFree tier with ads";
            default:
                return "Unknown";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
