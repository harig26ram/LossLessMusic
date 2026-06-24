package com.losslessmusic.app.data.api

import com.losslessmusic.app.data.models.*
import com.losslessmusic.app.data.models.response.*
import com.losslessmusic.app.domain.model.*
import com.losslessmusic.app.diag.CrashLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InnerTubeApi @Inject constructor() {
    private val logger = CrashLogger

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private var visitorData = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
    private val locale = YouTubeLocale(
        gl = Locale.getDefault().country.ifEmpty { "US" },
        hl = Locale.getDefault().toLanguageTag().ifEmpty { "en-US" }
    )

    private inline fun <reified T : Any> post(endpoint: String, body: T, clientConfig: YouTubeClient): String {
        val jsonBody = json.encodeToString(body)
        val request = Request.Builder()
            .url("${YouTubeClient.BASE_URL}$endpoint?key=${clientConfig.apiKey}&prettyPrint=false")
            .addHeader("Content-Type", YouTubeClient.JSON_MEDIA_TYPE)
            .addHeader("X-Goog-Api-Format-Version", "1")
            .addHeader("X-YouTube-Client-Name", clientConfig.clientName)
            .addHeader("X-YouTube-Client-Version", clientConfig.clientVersion)
            .addHeader("User-Agent", clientConfig.userAgent)
            .also { clientConfig.referer?.let { ref -> it.addHeader("Referer", ref) } }
            .post(jsonBody.toRequestBody(YouTubeClient.JSON_MEDIA_TYPE.toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val raw = response.body?.string()
        if (raw == null || raw.isBlank()) throw Exception("Empty response from $endpoint (${response.code})")
        if (!response.isSuccessful) throw Exception("HTTP ${response.code} for $endpoint: ${raw.take(200)}")
        return raw
    }

    suspend fun search(query: String, filter: String = BrowseFilter.FILTER_SONG): kotlin.Result<List<Song>> =
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val clientConfig = YouTubeClient.WEB_REMIX
                val raw = post("search", SearchBody(
                    context = clientConfig.toContext(locale, visitorData),
                    query = query, params = filter
                ), clientConfig)
                val response = json.decodeFromString<SearchResponse>(raw)
                parseSearchResponse(response)
            }.onFailure { logger.e("Search", it) }
        }

    suspend fun browse(browseId: String, params: String? = null): kotlin.Result<List<HomeFeedSection>> =
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val clientConfig = YouTubeClient.WEB_REMIX
                val raw = post("browse", BrowseBody(
                    context = clientConfig.toContext(locale, visitorData),
                    browseId = browseId, params = params
                ), clientConfig)
                val response = json.decodeFromString<BrowseResponse>(raw)
                parseBrowseResponse(response)
            }.onFailure { logger.e("Browse($browseId)", it) }
        }

    suspend fun getMoodCategories(): kotlin.Result<List<MoodCategory>> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val clientConfig = YouTubeClient.WEB_REMIX
            val raw = post("browse", BrowseBody(
                context = clientConfig.toContext(locale, visitorData),
                browseId = BrowseFilter.MOODS_AND_GENRES
            ), clientConfig)

            val root = json.parseToJsonElement(raw).jsonObject
            val sections = root["contents"]
                ?.jsonObject?.get("singleColumnMusicWatchNextResultsRenderer")
                ?.jsonObject?.get("tabbedRenderer")
                ?.jsonObject?.get("tabRenderer")
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("sectionListRenderer")
                ?.jsonObject?.get("contents")
                ?.jsonArray ?: throw Exception("No mood sections found")

            val moods = mutableListOf<MoodCategory>()
            for (section in sections) {
                val grid = section.jsonObject["gridRenderer"]?.jsonObject ?: continue
                val items = grid["items"]?.jsonArray ?: continue
                for (item in items) {
                    val nav = item.jsonObject["musicNavigationButtonRenderer"]?.jsonObject ?: continue
                    val text = nav["buttonText"]?.jsonObject?.get("runs")
                        ?.jsonArray?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content
                    val cmd = nav["clickCommand"]?.jsonObject ?: continue
                    val bid = cmd["browseEndpoint"]?.jsonObject?.get("browseId")?.jsonPrimitive?.content
                    val par = cmd["browseEndpoint"]?.jsonObject?.get("params")?.jsonPrimitive?.content
                    if (text != null && bid != null) moods.add(MoodCategory(text, bid, par))
                }
            }
            moods
        }.onFailure { logger.e("MoodCategories", it) }
    }

    suspend fun getPlayer(videoId: String): kotlin.Result<String> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val clientConfigs = listOf(
                YouTubeClient.ANDROID_MUSIC,
                YouTubeClient.IOS,
                YouTubeClient.ANDROID,
                YouTubeClient.TVHTML5,
            )
            var last: Exception? = null
            for (config in clientConfigs) {
                try {
                    val raw = post("player", PlayerBody(
                        context = config.toContext(locale, visitorData),
                        videoId = videoId
                    ), config)
                    val pr = json.decodeFromString<PlayerResponse>(raw)
                    val status = pr.playabilityStatus?.status
                    if (status == "OK") {
                        val formats = pr.streamingData?.adaptiveFormats
                        val url = formats?.firstOrNull { it.url != null }?.url
                        if (url != null) return@runCatching url
                        val cipher = formats?.firstOrNull { it.signatureCipher != null }?.signatureCipher
                        if (cipher != null) throw Exception("Stream needs signature decryption")

                        throw Exception("No playable stream from ${config.clientName}")
                    }
                    last = Exception("${config.clientName}: ${pr.playabilityStatus?.reason ?: status}")
                } catch (e: Exception) {
                    last = e
                    logger.w("Player(${config.clientName})", e.message)
                }
            }
            throw last ?: Exception("All player clients failed")
        }.onFailure { logger.e("Player", it) }
    }

    private fun parseSearchResponse(response: SearchResponse): List<Song> {
        val songs = mutableListOf<Song>()
        val tabs = response.contents?.tabbedSearchResultsRenderer?.tabs ?: return songs
        val tabContent = tabs.firstOrNull()?.tabRenderer?.content ?: return songs
        val sections = tabContent.sectionListRenderer?.contents ?: return songs

        for (section in sections) {
            val shelf = section.musicShelfRenderer
            if (shelf != null) {
                for (item in shelf.contents ?: emptyList()) {
                    val r = item.musicResponsiveListItemRenderer ?: continue
                    parseSongFromResponse(r)?.let { songs.add(it) }
                }
                continue
            }
            val itemSec = section.itemSectionRenderer
            if (itemSec != null) {
                for (item in itemSec.contents ?: emptyList()) {
                    val r = item.musicResponsiveListItemRenderer ?: continue
                    if (r.navigationEndpoint?.watchEndpoint?.videoId == null) continue
                    parseSongFromResponse(r)?.let { songs.add(it) }
                }
            }
        }
        return songs
    }

    private fun parseBrowseResponse(response: BrowseResponse): List<HomeFeedSection> {
        val sections = mutableListOf<HomeFeedSection>()

        fun navigateToSectionList(): List<BrowseResponse.SectionContent>? {
            val c = response.contents ?: return null
            c.sectionListRenderer?.let { return it.contents }
            c.tabbedSearchResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.let {
                return it.contents
            }
            c.singleColumnMusicWatchNextResultsRenderer?.tabbedRenderer?.tabRenderer?.content?.sectionListRenderer?.let {
                return it.contents
            }
            return null
        }

        val sectionList = navigateToSectionList() ?: return sections

        for (sectionContent in sectionList) {
            val carousel = sectionContent.musicCarouselShelfRenderer
            if (carousel != null) {
                val title = carousel.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text
                    ?: "Recommended"
                val items = mutableListOf<HomeFeedItem>()
                for (content in carousel.contents ?: emptyList()) {
                    val twoRow = content.musicTwoRowItemRenderer
                    if (twoRow != null) {
                        val t = twoRow.title?.runs?.firstOrNull()?.text ?: continue
                        val sub = twoRow.subtitle?.runs?.joinToString("") { it.text ?: "" } ?: ""
                        val thumb = twoRow.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                        val bid = twoRow.navigationEndpoint?.browseEndpoint?.browseId
                        val vid = twoRow.navigationEndpoint?.watchEndpoint?.videoId

                        if (vid != null) items.add(HomeFeedItem.SongItem(Song(vid, t, sub, thumbnailUrl = thumb)))
                        else if (bid != null) {
                            when {
                                bid.startsWith("MPRE") -> items.add(
                                    HomeFeedItem.AlbumItem(Album(bid, t, sub, thumbnailUrl = thumb)))
                                bid.startsWith("UC") -> items.add(
                                    HomeFeedItem.ArtistItem(Artist(bid, t, thumbnailUrl = thumb)))
                                bid.startsWith("VL") || bid.startsWith("PL") -> items.add(
                                    HomeFeedItem.PlaylistItem(com.losslessmusic.app.domain.model.Playlist(bid, t, thumbnailUrl = thumb)))
                            }
                        }
                    }
                    content.musicResponsiveListItemRenderer?.let { r ->
                        parseSongFromResponse(r)?.let { items.add(HomeFeedItem.SongItem(it)) }
                    }
                }
                if (items.isNotEmpty()) sections.add(HomeFeedSection(title, items))
            }

            val shelf = sectionContent.musicShelfRenderer
            if (shelf != null) {
                val title = shelf.title?.runs?.firstOrNull()?.text ?: "Songs"
                val items = shelf.contents?.mapNotNull {
                    it.musicResponsiveListItemRenderer?.let { r -> parseSongFromResponse(r)?.let { s -> HomeFeedItem.SongItem(s) } }
                } ?: emptyList()
                if (items.isNotEmpty()) sections.add(HomeFeedSection(title, items))
            }
        }
        return sections
    }

    private fun parseSongFromResponse(renderer: SearchResponse.MusicResponsiveListItemRenderer): Song? {
        val fc = renderer.flexColumns ?: return null
        if (fc.size < 2) return null
        val title = fc[0]?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text?.trim() ?: return null
        val artistRuns = fc.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        val artists = artistRuns?.joinToString("") { it.text?.trim() ?: "" } ?: "Unknown"
        val vid = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: return null
        val thumbs = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
        val thumbUrl = thumbs?.lastOrNull()?.url
        val dur = fc.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text
        val album = fc.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.getOrNull(1)?.text
        return Song(vid, title, artists, album, parseDuration(dur), thumbUrl)
    }

    private fun parseSongFromResponse(renderer: BrowseResponse.MusicResponsiveListItemRenderer): Song? {
        val fc = renderer.flexColumns ?: return null
        if (fc.size < 2) return null
        val title = fc[0]?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text?.trim() ?: return null
        val artistRuns = fc.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
        val artists = artistRuns?.joinToString("") { it.text?.trim() ?: "" } ?: "Unknown"
        val vid = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: return null
        val thumbs = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
        val thumbUrl = thumbs?.lastOrNull()?.url
        val dur = fc.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text
        val album = fc.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.getOrNull(1)?.text
        return Song(vid, title, artists, album, parseDuration(dur), thumbUrl)
    }

    private fun parseDuration(text: String?): Int? {
        if (text == null) return null
        val p = text.split(":")
        return when (p.size) {
            2 -> p[0].toIntOrNull()?.let { m -> p[1].toIntOrNull()?.let { s -> m * 60 + s } }
            3 -> p[0].toIntOrNull()?.let { h -> p[1].toIntOrNull()?.let { m -> p[2].toIntOrNull()?.let { s -> h * 3600 + m * 60 + s } } }
            else -> null
        }
    }
}
