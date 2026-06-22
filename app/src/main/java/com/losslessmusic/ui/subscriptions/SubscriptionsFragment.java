package com.losslessmusic.ui.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.losslessmusic.adapters.ServiceAdapter;
import com.losslessmusic.databinding.FragmentSubscriptionsBinding;
import com.losslessmusic.models.SubscriptionConfig;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionsFragment extends Fragment {

    private FragmentSubscriptionsBinding binding;
    private ServiceAdapter adapter;

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
                service.setLinked(enable);
                adapter.notifyDataSetChanged();
            }
        });

        loadServices();
    }

    private void loadServices() {
        List<SubscriptionConfig> services = new ArrayList<>();

        services.add(new SubscriptionConfig(SubscriptionConfig.ServiceType.LOCAL));
        services.add(new SubscriptionConfig(SubscriptionConfig.ServiceType.INTERNET_ARCHIVE));

        SubscriptionConfig jiosaavn = new SubscriptionConfig(SubscriptionConfig.ServiceType.JIOSAAVN);
        services.add(jiosaavn);

        SubscriptionConfig gaana = new SubscriptionConfig(SubscriptionConfig.ServiceType.GAANA);
        services.add(gaana);

        SubscriptionConfig spotify = new SubscriptionConfig(SubscriptionConfig.ServiceType.SPOTIFY);
        services.add(spotify);

        SubscriptionConfig youtubeMusic = new SubscriptionConfig(
                SubscriptionConfig.ServiceType.YOUTUBE_MUSIC);
        services.add(youtubeMusic);

        adapter.setServices(services);
    }

    private void showServiceDetails(SubscriptionConfig service) {
        if (service.getType().isAlwaysAvailable()) {
            return;
        }

        binding.serviceDetailCard.setVisibility(View.VISIBLE);
        binding.detailServiceName.setText(service.getServiceName());
        binding.detailServiceStatus.setText(service.isAvailable() ? "Connected" : "Not linked");
        binding.detailQualityLabel.setText(getQualityDescription(service.getType()));

        binding.linkServiceButton.setOnClickListener(v -> {
            service.setLinked(!service.isLinked());
            binding.detailServiceStatus.setText(
                    service.isAvailable() ? "Connected" : "Not linked");
            adapter.notifyDataSetChanged();
        });
    }

    private String getQualityDescription(SubscriptionConfig.ServiceType type) {
        switch (type) {
            case JIOSAAVN:
            case GAANA:
                return "Up to 320kbps AAC\nLossless unavailable on free tier";
            case SPOTIFY:
                return "Up to 320kbps Ogg Vorbis\nPremium required for highest quality";
            case YOUTUBE_MUSIC:
                return "Up to 256kbps AAC\nPremium required for high quality";
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
