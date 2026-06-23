package com.losslessmusic.app.data.models.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SearchResponse(
    val contents: Contents? = null,
    val continuationContents: ContinuationContents? = null,
) {
    @Serializable
    data class Contents(
        val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null,
    )

    @Serializable
    data class TabbedSearchResultsRenderer(
        val tabs: List<Tab>? = null,
    )

    @Serializable
    data class Tab(
        val tabRenderer: TabRenderer? = null,
    )

    @Serializable
    data class TabRenderer(
        val content: TabContent? = null,
    )

    @Serializable
    data class TabContent(
        val sectionListRenderer: SectionListRenderer? = null,
    )

    @Serializable
    data class SectionListRenderer(
        val contents: List<SectionContent>? = null,
    )

    @Serializable
    data class SectionContent(
        val musicShelfRenderer: MusicShelfRenderer? = null,
        val musicCardShelfRenderer: JsonElement? = null,
        val itemSectionRenderer: ItemSectionRenderer? = null,
    )

    @Serializable
    data class ItemSectionRenderer(
        val contents: List<ItemSectionContent>? = null,
    )

    @Serializable
    data class ItemSectionContent(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
    )

    @Serializable
    data class MusicShelfRenderer(
        val title: Title? = null,
        val contents: List<MusicShelfContent>? = null,
        val continuations: List<JsonElement>? = null,
    )

    @Serializable
    data class Title(
        val runs: List<Run>? = null,
    )

    @Serializable
    data class Run(
        val text: String? = null,
    )

    @Serializable
    data class MusicShelfContent(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
    )

    @Serializable
    data class MusicResponsiveListItemRenderer(
        val flexColumns: List<FlexColumn>? = null,
        val fixedColumns: List<FixedColumn>? = null,
        val thumbnail: ThumbnailRenderer? = null,
        val overlay: JsonElement? = null,
        val navigationEndpoint: NavigationEndpoint? = null,
    )

    @Serializable
    data class FlexColumn(
        val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null,
    )

    @Serializable
    data class FlexColumnRenderer(
        val text: Text? = null,
    )

    @Serializable
    data class Text(
        val runs: List<Run>? = null,
    )

    @Serializable
    data class FixedColumn(
        val musicResponsiveListItemFixedColumnRenderer: FixedColumnRenderer? = null,
    )

    @Serializable
    data class FixedColumnRenderer(
        val text: Text? = null,
    )

    @Serializable
    data class ThumbnailRenderer(
        val musicThumbnailRenderer: MusicThumbnailRenderer? = null,
    )

    @Serializable
    data class MusicThumbnailRenderer(
        val thumbnail: Thumbnail? = null,
    )

    @Serializable
    data class Thumbnail(
        val thumbnails: List<ThumbnailItem>? = null,
    )

    @Serializable
    data class ThumbnailItem(
        val url: String? = null,
        val width: Int? = null,
        val height: Int? = null,
    )

    @Serializable
    data class NavigationEndpoint(
        val watchEndpoint: WatchEndpoint? = null,
    )

    @Serializable
    data class WatchEndpoint(
        val videoId: String? = null,
        val playlistId: String? = null,
    )

    @Serializable
    data class ContinuationContents(
        val musicShelfContinuation: MusicShelfContinuation? = null,
    )

    @Serializable
    data class MusicShelfContinuation(
        val contents: List<MusicShelfContent>? = null,
        val continuations: List<JsonElement>? = null,
    )
}
