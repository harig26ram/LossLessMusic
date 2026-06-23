package com.losslessmusic.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private String id;
    private String title;
    private String artist;
    private String album;
    private long durationMs;
    private String artworkUrl;
    private Uri localUri;
    private AudioSource source;
    private AudioQuality quality;
    private String streamUrl;
    private String albumId;
    private String isrc;

    public enum AudioSource {
        LOCAL,
        INTERNET_ARCHIVE,
        JIOSAAVN,
        ITUNES,
        RADIO_BROWSER,
        GAANA,
        SPOTIFY_PREVIEW,
        YOUTUBE_MUSIC,
        UNKNOWN
    }

    public enum AudioQuality {
        LOSSLESS_24("FLAC 24-bit", 5),
        LOSSLESS_16("FLAC 16-bit", 4),
        HIGH_320("320kbps", 3),
        MEDIUM_256("256kbps", 2),
        LOW_128("128kbps", 1),
        UNKNOWN("Unknown", 0);

        private final String label;
        private final int rank;

        AudioQuality(String label, int rank) {
            this.label = label;
            this.rank = rank;
        }

        public String getLabel() { return label; }
        public int getRank() { return rank; }
    }

    public Song(String id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.quality = AudioQuality.UNKNOWN;
        this.source = AudioSource.UNKNOWN;
    }

    protected Song(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        durationMs = in.readLong();
        artworkUrl = in.readString();
        localUri = in.readParcelable(Uri.class.getClassLoader());
        source = AudioSource.valueOf(in.readString());
        quality = AudioQuality.valueOf(in.readString());
        streamUrl = in.readString();
        albumId = in.readString();
        isrc = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeLong(durationMs);
        dest.writeString(artworkUrl);
        dest.writeParcelable(localUri, flags);
        dest.writeString(source.name());
        dest.writeString(quality.name());
        dest.writeString(streamUrl);
        dest.writeString(albumId);
        dest.writeString(isrc);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getArtworkUrl() { return artworkUrl; }
    public void setArtworkUrl(String artworkUrl) { this.artworkUrl = artworkUrl; }

    public Uri getLocalUri() { return localUri; }
    public void setLocalUri(Uri localUri) { this.localUri = localUri; }

    public AudioSource getSource() { return source; }
    public void setSource(AudioSource source) { this.source = source; }

    public AudioQuality getQuality() { return quality; }
    public void setQuality(AudioQuality quality) { this.quality = quality; }

    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    public String getAlbumId() { return albumId; }
    public void setAlbumId(String albumId) { this.albumId = albumId; }

    public String getIsrc() { return isrc; }
    public void setIsrc(String isrc) { this.isrc = isrc; }

    public String getDurationFormatted() {
        long totalSec = durationMs / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }

    public String getSourceQualityLabel() {
        return source.name() + " @ " + quality.getLabel();
    }
}
