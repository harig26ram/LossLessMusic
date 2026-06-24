package com.losslessmusic.app.data.models

sealed class HomeFeedItem {
    data class SongItem(val song: Song) : HomeFeedItem()
    data class AlbumItem(val album: Album) : HomeFeedItem()
    data class ArtistItem(val artist: Artist) : HomeFeedItem()
    data class PlaylistItem(val playlist: Playlist) : HomeFeedItem()
}

data class HomeFeedSection(
    val title: String,
    val items: List<HomeFeedItem>,
)
