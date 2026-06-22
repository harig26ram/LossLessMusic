package com.losslessmusic.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.losslessmusic.R;
import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends ListAdapter<Song, SongAdapter.SongViewHolder> {

    private OnSongClickListener listener;
    private int highlightIndex = -1;
    private List<Song> cachedList = new ArrayList<>();

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
        void onSongLongClick(Song song, int position);
    }

    private static final DiffUtil.ItemCallback<Song> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Song>() {
                @Override
                public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle())
                            && oldItem.getArtist().equals(newItem.getArtist());
                }
            };

    public SongAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(@Nullable List<Song> list) {
        super.submitList(list);
        cachedList = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<Song> getCurrentList() {
        return cachedList;
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setHighlightIndex(int index) {
        int old = highlightIndex;
        highlightIndex = index;
        if (old >= 0) notifyItemChanged(old);
        if (index >= 0) notifyItemChanged(index);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = getItem(position);
        holder.bind(song, position == highlightIndex);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSongClick(song, holder.getAdapterPosition());
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onSongLongClick(song, holder.getAdapterPosition());
            return true;
        });
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView artistText;
        private final TextView durationText;
        private final TextView qualityBadge;
        private final ImageView artwork;
        private final View nowPlayingIndicator;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.songTitle);
            artistText = itemView.findViewById(R.id.songArtist);
            durationText = itemView.findViewById(R.id.songDuration);
            qualityBadge = itemView.findViewById(R.id.qualityBadge);
            artwork = itemView.findViewById(R.id.songArtwork);
            nowPlayingIndicator = itemView.findViewById(R.id.nowPlayingIndicator);
        }

        void bind(Song song, boolean isHighlighted) {
            titleText.setText(song.getTitle());
            artistText.setText(song.getArtist());
            durationText.setText(song.getDurationFormatted());

            String qualityLabel = song.getQuality().getLabel();
            qualityBadge.setText(qualityLabel);

            GradientDrawable badgeBg = (GradientDrawable) qualityBadge.getBackground();
            if (badgeBg != null) {
                if (song.getQuality().getRank() >= 4) {
                    badgeBg.setColor(itemView.getContext().getColor(R.color.quality_lossless));
                } else if (song.getQuality().getRank() >= 3) {
                    badgeBg.setColor(itemView.getContext().getColor(R.color.quality_high));
                } else {
                    badgeBg.setColor(itemView.getContext().getColor(R.color.quality_standard));
                }
            }

            if (isHighlighted) {
                nowPlayingIndicator.setVisibility(View.VISIBLE);
                titleText.setTextColor(itemView.getContext().getColor(R.color.color_primary));
            } else {
                nowPlayingIndicator.setVisibility(View.GONE);
                titleText.setTextColor(itemView.getContext().getColor(R.color.text_primary));
            }

            Glide.with(itemView.getContext())
                    .load(song.getArtworkUrl())
                    .placeholder(R.drawable.ic_music_note)
                    .centerCrop()
                    .into(artwork);
        }
    }
}
