package com.losslessmusic.audio;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.losslessmusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class LocalFileProvider implements AudioSourceProvider {

    private final Context context;

    public LocalFileProvider(Context context) {
        this.context = context;
    }

    @Override
    public String getProviderName() { return "Local Files"; }

    @Override
    public Song.AudioSource getSourceType() { return Song.AudioSource.LOCAL; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public boolean isPremiumRequired() { return false; }

    @Override
    public Song.AudioQuality getMaxQuality() { return Song.AudioQuality.LOSSLESS_24; }

    @Override
    public void search(String query, SearchResultCallback callback) {
        List<Song> results = queryLocalAudio(query);
        callback.onResults(results);
    }

    @Override
    public void getAlbumTracks(String albumId, SearchResultCallback callback) {
        List<Song> tracks = queryAlbumTracks(albumId);
        callback.onResults(tracks);
    }

    @Override
    public void getTrending(TrendingCallback callback) {
        List<Song> recent = queryRecentTracks(50);
        callback.onResults(recent, "Recently Added");
    }

    private List<Song> queryLocalAudio(String query) {
        List<Song> songs = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection = new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.MIME_TYPE
            };
        } else {
            projection = new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
            };
        }

        if (query != null && !query.isEmpty()) {
            selection += " AND (" +
                    MediaStore.Audio.Media.TITLE + " LIKE ? OR " +
                    MediaStore.Audio.Media.ARTIST + " LIKE ? OR " +
                    MediaStore.Audio.Media.ALBUM + " LIKE ?)";
        }

        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] selectionArgs = query != null ?
                new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"} :
                null;

        try (Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, sortOrder)) {

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    String id = cursor.getString(idCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    long albumId = cursor.getLong(albumIdCol);
                    long duration = cursor.getLong(durationCol);

                    Uri contentUri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                    Song song = new Song(id, title, artist);
                    song.setAlbum(album);
                    song.setDurationMs(duration);
                    song.setLocalUri(contentUri);
                    song.setSource(Song.AudioSource.LOCAL);
                    song.setQuality(Song.AudioQuality.LOSSLESS_24);
                    song.setAlbumId(String.valueOf(albumId));
                    songs.add(song);
                }
            }
        }

        return songs;
    }

    private List<Song> queryAlbumTracks(String albumId) {
        List<Song> songs = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.ALBUM_ID + " = ?";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
        };

        try (Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, new String[]{albumId},
                MediaStore.Audio.Media.TRACK + " ASC")) {

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    String id = cursor.getString(idCol);
                    Uri contentUri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    Song song = new Song(id, cursor.getString(titleCol),
                            cursor.getString(artistCol));
                    song.setAlbum(cursor.getString(albumCol));
                    song.setDurationMs(cursor.getLong(durationCol));
                    song.setLocalUri(contentUri);
                    song.setSource(Song.AudioSource.LOCAL);
                    song.setQuality(Song.AudioQuality.LOSSLESS_24);
                    songs.add(song);
                }
            }
        }

        return songs;
    }

    private List<Song> queryRecentTracks(int limit) {
        List<Song> songs = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };

        try (Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int count = 0;
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext() && count < limit) {
                    String id = cursor.getString(idCol);
                    Uri contentUri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    Song song = new Song(id, cursor.getString(titleCol),
                            cursor.getString(artistCol));
                    song.setAlbum(cursor.getString(albumCol));
                    song.setDurationMs(cursor.getLong(durationCol));
                    song.setLocalUri(contentUri);
                    song.setSource(Song.AudioSource.LOCAL);
                    song.setQuality(Song.AudioQuality.LOSSLESS_24);
                    songs.add(song);
                    count++;
                }
            }
        }

        return songs;
    }
}
