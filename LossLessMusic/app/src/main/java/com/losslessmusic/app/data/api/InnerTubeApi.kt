package com.losslessmusic.app.data.api

import com.losslessmusic.app.data.models.Song
import com.losslessmusic.app.data.models.YouTubeClient
import com.losslessmusic.app.data.models.YouTubeLocale
import com.losslessmusic.app.data.models.body.PlayerBody
import com.losslessmusic.app.data.models.body.SearchBody
import com.losslessmusic.app.data.models.response.PlayerResponse
import com.losslessmusic.app.data.models.response.SearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import java.util.Locale
import java.util.concurrent.TimeUnit

object InnerTubeApi {
    private const val BASE_URL = "https://music.youtube.com/youtubei/v1/"
    private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    private const val DEFAULT_VISITOR_DATA = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
    private const val FILTER_SONG = "EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D"

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

    suspend fun search(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val clientConfig = YouTubeClient.WEB_REMIX
            val body = SearchBody(
                context = clientConfig.toContext(locale, visitorData),
                query = query,
                params = FILTER_SONG
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
            parseSearchResponse(searchResponse)
        }
    }

    suspend fun getStreamUrl(videoId: String): Result<PlayerResponse> = withContext(Dispatchers.IO) {
        runCatching {
            var lastError: Exception? = null

            val clients = listOf(
                YouTubeClient.ANDROID_MUSIC to false,
                YouTubeClient.IOS to true,
                YouTubeClient.TVHTML5 to false,
            )

            for ((clientConfig, hasOsVersion) in clients) {
                try {
                    val ctx = clientConfig.toContext(locale, visitorData).let {
                        if (hasOsVersion && clientConfig.osVersion != null) it
                        else it
                    }

                    val body = PlayerBody(
                        context = ctx,
                        videoId = videoId
                    )

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
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    val playerResponse = json.decodeFromString(PlayerResponse.serializer(), responseBody)

                    if (playerResponse.playabilityStatus?.status == "OK") {
                        val formats = playerResponse.streamingData?.adaptiveFormats
                        if (formats.isNullOrEmpty()) {
                            lastError = Exception("No adaptive formats in response")
                            continue
                        }
                        val hasUrl = formats.any { !it.url.isNullOrBlank() }
                        if (!hasUrl) {
                            lastError = Exception("Stream URLs require decryption (n-param)")
                            continue
                        }
                        return@runCatching playerResponse
                    }

                    lastError = Exception(playerResponse.playabilityStatus?.reason ?: "Status not OK")
                } catch (e: Exception) {
                    lastError = e
                }
            }

            throw lastError ?: Exception("All player clients failed")
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
            if (!response.isSuccessful) {
                throw Exception("Piped API returned ${response.code}")
            }
            val responseBody = response.body?.string() ?: throw Exception("Empty response")

            val jsonObj = json.parseToJsonElement(responseBody).jsonObject
            val audioStreams = jsonObj["audioStreams"] as? JsonArray ?: throw Exception("No audio streams")

            val bestStream = audioStreams.maxByOrNull { element ->
                (element as JsonObject)["bitrate"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            } as? JsonObject

            bestStream?.get("url")?.jsonPrimitive?.content
                ?: throw Exception("No stream URL found")
        }
    }

    private fun parseSearchResponse(response: SearchResponse): List<Song> {
        val songs = mutableListOf<Song>()

        val tabs = response.contents?.tabbedSearchResultsRenderer?.tabs ?: return songs
        val tabContent = tabs.firstOrNull()?.tabRenderer?.content ?: return songs
        val sections = tabContent.sectionListRenderer?.contents ?: return songs

        for (section in sections) {
            if (section.musicShelfRenderer != null) {
                val items = section.musicShelfRenderer.contents ?: continue
                for (item in items) {
                    val renderer = item.musicResponsiveListItemRenderer ?: continue
                    val song = parseSongItem(renderer) ?: continue
                    songs.add(song)
                }
            }

            if (section.itemSectionRenderer != null) {
                val items = section.itemSectionRenderer.contents ?: continue
                for (item in items) {
                    val renderer = item.musicResponsiveListItemRenderer ?: continue
                    if (renderer.navigationEndpoint?.watchEndpoint?.videoId == null) continue
                    val song = parseSongItem(renderer) ?: continue
                    songs.add(song)
                }
            }
        }

        return songs
    }

    private fun parseSongItem(renderer: SearchResponse.MusicResponsiveListItemRenderer): Song? {
        val flexColumns = renderer.flexColumns ?: return null
        if (flexColumns.size < 2) return null

        val titleRun = flexColumns[0]
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.firstOrNull()
        val title = titleRun?.text?.trim() ?: return null

        val artistRuns = flexColumns.getOrNull(1)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
        val artists = artistRuns?.joinToString("") { it.text?.trim() ?: "" } ?: "Unknown Artist"

        val videoId = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: return null

        val thumbnails = renderer.thumbnail
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
        val thumbnailUrl = thumbnails?.lastOrNull()?.url

        val durationText = flexColumns.getOrNull(2)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.firstOrNull()
            ?.text

        val album = flexColumns.getOrNull(2)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text
            ?.runs
            ?.getOrNull(1)
            ?.text

        return Song(
            id = videoId,
            title = title,
            artists = artists,
            album = album,
            thumbnailUrl = thumbnailUrl,
            duration = parseDuration(durationText),
        )
    }

    private fun parseDuration(text: String?): Int? {
        if (text == null) return null
        val parts = text.split(":")
        return when (parts.size) {
            2 -> parts[0].toIntOrNull()?.let { m ->
                parts[1].toIntOrNull()?.let { s -> m * 60 + s }
            }
            3 -> parts[0].toIntOrNull()?.let { h ->
                parts[1].toIntOrNull()?.let { m ->
                    parts[2].toIntOrNull()?.let { s -> h * 3600 + m * 60 + s }
                }
            }
            else -> null
        }
    }
}
