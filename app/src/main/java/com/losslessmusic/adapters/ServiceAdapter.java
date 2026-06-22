package com.losslessmusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.losslessmusic.R;
import com.losslessmusic.models.SubscriptionConfig;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<SubscriptionConfig> services = new ArrayList<>();
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(SubscriptionConfig service);
        void onToggleService(SubscriptionConfig service, boolean enable);
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void setServices(List<SubscriptionConfig> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        SubscriptionConfig service = services.get(position);
        holder.bind(service);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onServiceClick(service);
        });

        holder.toggleSwitch.setOnCheckedChangeListener(null);
        holder.toggleSwitch.setChecked(service.isLinked());
        holder.toggleSwitch.setOnCheckedChangeListener((btn, checked) -> {
            if (listener != null) listener.onToggleService(service, checked);
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView serviceName;
        private final TextView serviceStatus;
        private final TextView serviceQuality;
        private final ImageView serviceIcon;
        private final com.google.android.material.switchmaterial.SwitchMaterial toggleSwitch;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceStatus = itemView.findViewById(R.id.serviceStatus);
            serviceQuality = itemView.findViewById(R.id.serviceQuality);
            serviceIcon = itemView.findViewById(R.id.serviceIcon);
            toggleSwitch = itemView.findViewById(R.id.serviceToggle);
        }

        void bind(SubscriptionConfig service) {
            serviceName.setText(service.getServiceName());

            if (!service.isEnabled()) {
                serviceStatus.setText("Unavailable");
                serviceStatus.setTextColor(itemView.getContext().getColor(R.color.error_color));
                toggleSwitch.setEnabled(false);
                toggleSwitch.setChecked(false);
            } else if (service.isAvailable()) {
                serviceStatus.setText("Connected");
                serviceStatus.setTextColor(itemView.getContext().getColor(R.color.color_primary));
                toggleSwitch.setEnabled(true);
                toggleSwitch.setChecked(true);
            } else if (service.getType().isAlwaysAvailable()) {
                serviceStatus.setText("Available");
                serviceStatus.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                toggleSwitch.setEnabled(false);
                toggleSwitch.setChecked(true);
            } else {
                serviceStatus.setText("Not linked");
                serviceStatus.setTextColor(itemView.getContext().getColor(R.color.text_hint));
                toggleSwitch.setEnabled(true);
                toggleSwitch.setChecked(false);
            }

            String quality = getMaxQualityLabel(service.getType());
            serviceQuality.setText(quality);

            int iconRes = getServiceIcon(service.getType());
            Glide.with(itemView.getContext())
                    .load(iconRes)
                    .into(serviceIcon);
        }

        private String getMaxQualityLabel(SubscriptionConfig.ServiceType type) {
            switch (type) {
                case LOCAL:
                    return "FLAC 24-bit";
                case INTERNET_ARCHIVE:
                    return "FLAC 16-bit";
                case JIOSAAVN:
                    return "30s Preview";
                case GAANA:
                case SPOTIFY:
                    return "320kbps Ogg";
                case YOUTUBE_MUSIC:
                    return "Temporarily Down";
                default:
                    return "Unknown";
            }
        }

        private int getServiceIcon(SubscriptionConfig.ServiceType type) {
            switch (type) {
                case LOCAL:
                    return R.drawable.ic_phone;
                case INTERNET_ARCHIVE:
                    return R.drawable.ic_cloud;
                case JIOSAAVN:
                    return R.drawable.ic_headphones;
                case GAANA:
                    return R.drawable.ic_headphones;
                case SPOTIFY:
                    return R.drawable.ic_headphones;
                default:
                    return R.drawable.ic_music_note;
            }
        }
    }
}
