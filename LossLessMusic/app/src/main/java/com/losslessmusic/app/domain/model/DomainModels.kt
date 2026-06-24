package com.losslessmusic.app.domain.model

data class Song(
    val id: String,
    val title: String,
    val artists: String,
    val album: String? = null,
    val durationSeconds: Int? = null,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
    val isExplicit: Boolean = false,
    val videoType: String = "MUSIC_VIDEO_TYPE_OMV"
)

data class Album(
    val id: String,
    val title: String,
    val artist: String? = null,
    val year: Int? = null,
    val thumbnailUrl: String? = null,
    val songCount: Int? = null,
    val browseId: String? = null
)

data class Artist(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val subscriberCount: String? = null
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val songCount: Int? = null,
    val author: String? = null
)

data class HomeFeedSection(
    val title: String,
    val items: List<HomeFeedItem>
)

sealed class HomeFeedItem {
    data class SongItem(val song: Song) : HomeFeedItem()
    data class AlbumItem(val album: Album) : HomeFeedItem()
    data class ArtistItem(val artist: Artist) : HomeFeedItem()
    data class PlaylistItem(val playlist: Playlist) : HomeFeedItem()
}

data class MoodCategory(
    val title: String,
    val browseId: String,
    val params: String? = null
)

