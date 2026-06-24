package com.losslessmusic.app.data.api

import com.losslessmusic.app.data.models.*
import com.losslessmusic.app.data.models.body.BrowseBody
import com.losslessmusic.app.data.models.body.PlayerBody
import com.losslessmusic.app.data.models.body.SearchBody
import com.losslessmusic.app.data.models.response.BrowseResponse
import com.losslessmusic.app.data.models.response.PlayerResponse
import com.losslessmusic.app.data.models.response.SearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import java.util.concurrent.TimeUnit

object InnerTubeApi {
    private const val BASE_URL = "https://music.youtube.com/youtubei/v1/"
    private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    private const val DEFAULT_VISITOR_DATA = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private var visitorData = DEFAULT_VISITOR_DATA
    private val locale = YouTubeLocale(
        gl = Locale.getDefault().country.ifEmpty { "US" },
        hl = Locale.getDefault().toLanguageTag().ifEmpty { "en-US" }
    )

    suspend fun search(query: String, filter: String? = BrowseEndpoints.FILTER_SONG): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val clientConfig = YouTubeClient.WEB_REMIX
            val body = SearchBody(
                context = clientConfig.toContext(locale, visitorData),
                query = query,
                params = filter
            )
            val jsonBody = json.encodeToString(SearchBody.serializer(), body)
            val request = Request.Builder()
                .url("${BASE_URL}search?key=${clientConfig.apiKey}&prettyPrint=false")
                .addHeader("Content-Type", JSON_MEDIA_TYPE)
                .addHeader("X-Goog-Api-Format-Version", "1")
                .addHeader("X-YouTube-Client-Name", clientConfig.clientName)
                .addHeader("X-YouTube-Client-Version", clientConfig.clientVersion)
                .addHeader("x-origin", "https://music.youtube.com")
                .addHeader("Referer", "https://music.youtube.com/")
                .addHeader("User-Agent", clientConfig.userAgent)
                .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            val searchResponse = json.decodeFromString(SearchResponse.serializer(), responseBody)
            parseSearchResults(searchResponse)
        }
    }

    suspend fun browse(browseId: String, params: String? = null): Result<List<HomeFeedSection>> = withContext(Dispatchers.IO) {
        runCatching {
            val clientConfig = YouTubeClient.WEB_REMIX
            val body = BrowseBody(
                context = clientConfig.toContext(locale, visitorData),
                browseId = browseId,
                params = params
            )
            val jsonBody = json.encodeToString(BrowseBody.serializer(), body)
            val request = Request.Builder()
                .url("${BASE_URL}browse?key=${clientConfig.apiKey}&prettyPrint=false")
                .addHeader("Content-Type", JSON_MEDIA_TYPE)
                .addHeader("X-Goog-Api-Format-Version", "1")
                .addHeader("X-YouTube-Client-Name", clientConfig.clientName)
                .addHeader("X-YouTube-Client-Version", clientConfig.clientVersion)
                .addHeader("x-origin", "https://music.youtube.com")
                .addHeader("Referer", "https://music.youtube.com/")
                .addHeader("User-Agent", clientConfig.userAgent)
                .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            val browseResponse = json.decodeFromString(BrowseResponse.serializer(), responseBody)
            parseBrowseResponse(browseResponse)
        }
    }

    suspend fun getMoodCategories(): Result<List<MoodCategory>> = withContext(Dispatchers.IO) {
        runCatching {
            val sections = browse(BrowseEndpoints.MOODS_AND_GENRES).getOrThrow()
            val moods = mutableListOf<MoodCategory>()
            for (section in sections) {
                for (item in section.items) {
                    if (item is HomeFeedItem.PlaylistItem) {
                        // Each mood/genre is a navigation button in the grid
                    }
                }
            }

            // For moods, the response might have musicNavigationButtonRenderer in gridRenderer
            val clientConfig = YouTubeClient.WEB_REMIX
            val body = BrowseBody(
                context = clientConfig.toContext(locale, visitorData),
                browseId = BrowseEndpoints.MOODS_AND_GENRES
            )
            val jsonBody = json.encodeToString(BrowseBody.serializer(), body)
            val request = Request.Builder()
                .url("${BASE_URL}browse?key=${clientConfig.apiKey}&prettyPrint=false")
                .addHeader("Content-Type", JSON_MEDIA_TYPE)
                .addHeader("X-Goog-Api-Format-Version", "1")
                .addHeader("X-YouTube-Client-Name", clientConfig.clientName)
                .addHeader("X-YouTube-Client-Version", clientConfig.clientVersion)
                .addHeader("x-origin", "https://music.youtube.com")
                .addHeader("Referer", "https://music.youtube.com/")
                .addHeader("User-Agent", clientConfig.userAgent)
                .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val raw = response.body?.string() ?: throw Exception("Empty")

            // Parse mood categories from gridRenderer items
            val root = json.parseToJsonElement(raw).jsonObject
            val sections2 = root["contents"]
                ?.jsonObject
                ?.get("singleColumnMusicWatchNextResultsRenderer")
                ?.jsonObject
                ?.get("tabbedRenderer")
                ?.jsonObject
                ?.get("tabRenderer")
                ?.jsonObject
                ?.get("content")
                ?.jsonObject
                ?.get("sectionListRenderer")
                ?.jsonObject
                ?.get("contents")
                ?.jsonArray

            if (sections2 != null) {
                for (section in sections2) {
                    val gridRenderer = section.jsonObject["gridRenderer"]?.jsonObject
                    if (gridRenderer != null) {
                        val items = gridRenderer["items"]?.jsonArray ?: continue
                        for (item in items) {
                            val navRenderer = item.jsonObject["musicNavigationButtonRenderer"]?.jsonObject ?: continue
                            val text = navRenderer["buttonText"]?.jsonObject?.get("runs")
                                ?.jsonArray?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content
                            val clickCmd = navRenderer["clickCommand"]?.jsonObject ?: continue
                            val browseId2 = clickCmd["browseEndpoint"]?.jsonObject?.get("browseId")?.jsonPrimitive?.content
                            val params2 = clickCmd["browseEndpoint"]?.jsonObject?.get("params")?.jsonPrimitive?.content
                            if (text != null && browseId2 != null) {
                                moods.add(MoodCategory(text, browseId2, params2))
                            }
                        }
                    }
                }
            }
            moods
        }
    }

    suspend fun getMoodPlaylists(browseId: String, params: String?): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val sections = browse(browseId, params).getOrThrow()
            val songs = mutableListOf<Song>()
            for (section in sections) {
                for (item in section.items) {
                    if (item is HomeFeedItem.SongItem) {
                        songs.add(item.song)
                    }
                }
            }
            songs
        }
    }

    suspend fun getStreamUrl(videoId: String): Result<PlayerResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val clients = listOf(
                YouTubeClient.ANDROID_MUSIC to false,
                YouTubeClient.IOS to true,
                YouTubeClient.TVHTML5 to false,
            )
            var lastError: Exception? = null
            for ((clientConfig, hasOsVersion) in clients) {
                try {
                    val ctx = clientConfig.toContext(locale, visitorData)
                    val body = PlayerBody(context = ctx, videoId = videoId)
                    val jsonBody = json.encodeToString(PlayerBody.serializer(), body)
                    val requestBuilder = Request.Builder()
                        .url("${BASE_URL}player?key=${clientConfig.apiKey}&prettyPrint=false")
                        .addHeader("Content-Type", JSON_MEDIA_TYPE)
                        .addHeader("X-Goog-Api-Format-Version", "1")
                        .addHeader("X-YouTube-Client-Name", clientConfig.clientName)
                        .addHeader("X-YouTube-Client-Version", clientConfig.clientVersion)
                        .addHeader("x-origin", "https://music.youtube.com")
                        .addHeader("User-Agent", clientConfig.userAgent)
                        .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
                    if (clientConfig.referer != null) {
                        requestBuilder.addHeader("Referer", clientConfig.referer)
                    }
                    val response = client.newCall(requestBuilder.build()).execute()
                    val responseBody = response.body?.string() ?: throw Exception("Empty")
                    val playerResponse = json.decodeFromString(PlayerResponse.serializer(), responseBody)
                    if (playerResponse.playabilityStatus?.status == "OK") {
                        val formats = playerResponse.streamingData?.adaptiveFormats
                        if (formats.isNullOrEmpty()) {
                            lastError = Exception("No adaptive formats")
                            continue
                        }
                        if (formats.any { !it.url.isNullOrBlank() }) {
                            return@runCatching playerResponse
                        }
                        lastError = Exception("Stream URLs need decryption (n-param)")
                        continue
                    }
                    lastError = Exception(playerResponse.playabilityStatus?.reason ?: "Status not OK")
                } catch (e: Exception) {
                    lastError = e
                }
            }
            throw lastError ?: Exception("All players failed")
        }
    }

    suspend fun getPipedStream(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://pipedapi.kavin.rocks/streams/$videoId")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "LossLessMusic/1.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Piped returned ${response.code}")
            val body = response.body?.string() ?: throw Exception("Empty")
            val obj = json.parseToJsonElement(body).jsonObject
            val streams = obj["audioStreams"] as? JsonArray ?: throw Exception("No audio streams")
            val best = streams.maxByOrNull { e ->
                (e as JsonObject)["bitrate"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            } as? JsonObject
            best?.get("url")?.jsonPrimitive?.content ?: throw Exception("No URL")
        }
    }

    private fun parseSearchResults(response: SearchResponse): List<Song> {
        val songs = mutableListOf<Song>()
        val tabs = response.contents?.tabbedSearchResultsRenderer?.tabs ?: return songs
        val tabContent = tabs.firstOrNull()?.tabRenderer?.content ?: return songs
        val sections = tabContent.sectionListRenderer?.contents ?: return songs
        for (section in sections) {
            if (section.musicShelfRenderer != null) {
                val items = section.musicShelfRenderer.contents ?: continue
                for (item in items) {
                    val renderer = item.musicResponsiveListItemRenderer ?: continue
                    parseSongItem(renderer)?.let { songs.add(it) }
                }
            }
            if (section.itemSectionRenderer != null) {
                val items = section.itemSectionRenderer.contents ?: continue
                for (item in items) {
                    val renderer = item.musicResponsiveListItemRenderer ?: continue
                    if (renderer.navigationEndpoint?.watchEndpoint?.videoId == null) continue
                    parseSongItem(renderer)?.let { songs.add(it) }
                }
            }
        }
        return songs
    }

    private fun parseBrowseResponse(response: BrowseResponse): List<HomeFeedSection> {
        val sections = mutableListOf<HomeFeedSection>()

        val rawContents = response.contents
        if (rawContents == null) return sections

        val sectionList = rawContents.sectionListRenderer?.contents
            ?: rawContents.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: rawContents.singleColumnMusicWatchNextResultsRenderer?.tabbedRenderer
                ?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: return sections

        for (sectionContent in sectionList) {
            val carousel = sectionContent.musicCarouselShelfRenderer
            if (carousel != null) {
                val title = carousel.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text ?: "Recommended"
                val items = mutableListOf<HomeFeedItem>()
                for (content in carousel.contents ?: emptyList()) {
                    val twoRow = content.musicTwoRowItemRenderer
                    if (twoRow != null) {
                        val itemTitle = twoRow.title?.runs?.firstOrNull()?.text ?: continue
                        val subtitle = twoRow.subtitle?.runs?.joinToString("") { it.text ?: "" } ?: ""
                        val thumbUrl = twoRow.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                        val browseId = twoRow.navigationEndpoint?.browseEndpoint?.browseId
                        val videoId = twoRow.navigationEndpoint?.watchEndpoint?.videoId

                        if (videoId != null) {
                            items.add(HomeFeedItem.SongItem(Song(id = videoId, title = itemTitle, artists = subtitle, thumbnailUrl = thumbUrl)))
                        } else if (browseId != null) {
                            if (browseId.startsWith("MPRE")) {
                                items.add(HomeFeedItem.AlbumItem(Album(id = browseId, title = itemTitle, artist = subtitle, thumbnailUrl = thumbUrl)))
                            } else if (browseId.startsWith("UC")) {
                                items.add(HomeFeedItem.ArtistItem(Artist(id = browseId, name = itemTitle, thumbnailUrl = thumbUrl)))
                            } else if (browseId.startsWith("VL") || browseId.startsWith("PL")) {
                                items.add(HomeFeedItem.PlaylistItem(Playlist(id = browseId, title = itemTitle, thumbnailUrl = thumbUrl)))
                            }
                        }
                    }
                    val listItem = content.musicResponsiveListItemRenderer
                    if (listItem != null) {
                        parseSongItem(listItem)?.let { items.add(HomeFeedItem.SongItem(it)) }
                    }
                }
                if (items.isNotEmpty()) sections.add(HomeFeedSection(title, items))
            }

            val shelf = sectionContent.musicShelfRenderer
            if (shelf != null) {
                val title = shelf.title?.runs?.firstOrNull()?.text ?: "Songs"
                val items = mutableListOf<HomeFeedItem>()
                for (item in shelf.contents ?: emptyList()) {
                    val renderer = item.musicResponsiveListItemRenderer ?: continue
                    parseSongItem(renderer)?.let { items.add(HomeFeedItem.SongItem(it)) }
                }
                if (items.isNotEmpty()) sections.add(HomeFeedSection(title, items))
            }
        }
        return sections
    }

    private fun parseSongItem(renderer: BrowseResponse.MusicResponsiveListItemRenderer): Song? {
        val flexColumns = renderer.flexColumns ?: return null
        if (flexColumns.size < 2) return null
        val title = flexColumns[0]?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text?.trim() ?: return null
        val artistRuns = flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        val artists = artistRuns?.joinToString("") { it.text?.trim() ?: "" } ?: "Unknown Artist"
        val videoId = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: return null
        val thumbs = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
        val thumbUrl = thumbs?.lastOrNull()?.url
        val durationText = flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text
        val album = flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.getOrNull(1)?.text
        return Song(id = videoId, title = title, artists = artists, album = album, thumbnailUrl = thumbUrl, duration = parseDuration(durationText))
    }

    private fun parseSongItem(renderer: SearchResponse.MusicResponsiveListItemRenderer): Song? {
        val flexColumns = renderer.flexColumns ?: return null
        if (flexColumns.size < 2) return null
        val title = flexColumns[0]?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text?.trim() ?: return null
        val artistRuns = flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        val artists = artistRuns?.joinToString("") { it.text?.trim() ?: "" } ?: "Unknown Artist"
        val videoId = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: return null
        val thumbs = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
        val thumbUrl = thumbs?.lastOrNull()?.url
        val durationText = flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text
        val album = flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.getOrNull(1)?.text
        return Song(id = videoId, title = title, artists = artists, album = album, thumbnailUrl = thumbUrl, duration = parseDuration(durationText))
    }

    private fun parseDuration(text: String?): Int? {
        if (text == null) return null
        val parts = text.split(":")
        return when (parts.size) {
            2 -> parts[0].toIntOrNull()?.let { m -> parts[1].toIntOrNull()?.let { s -> m * 60 + s } }
            3 -> parts[0].toIntOrNull()?.let { h -> parts[1].toIntOrNull()?.let { m -> parts[2].toIntOrNull()?.let { s -> h * 3600 + m * 60 + s } } }
            else -> null
        }
    }
}
