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

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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
                query = query
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
            val responseBody = response.body?.string() ?: throw Exception("Empty response")

            val searchResponse = json.decodeFromString(SearchResponse.serializer(), responseBody)
            parseSearchResponse(searchResponse)
        }
    }

    suspend fun getStreamUrl(videoId: String): Result<PlayerResponse> = withContext(Dispatchers.IO) {
        runCatching {
            var lastError: Exception? = null

            val clients = listOf(
                YouTubeClient.ANDROID_MUSIC,
                YouTubeClient.IOS,
                YouTubeClient.TVHTML5
            )

            for (clientConfig in clients) {
                try {
                    val body = PlayerBody(
                        context = clientConfig.toContext(locale, visitorData),
                        videoId = videoId
                    )

                    val jsonBody = json.encodeToString(PlayerBody.serializer(), body)
                    val request = Request.Builder()
                        .url("${BASE_URL}player?key=${clientConfig.apiKey}&prettyPrint=false")
                        .addHeader("Content-Type", JSON_MEDIA_TYPE)
                        .addHeader("X-Goog-Api-Format-Version", "1")
                        .addHeader("X-YouTube-Client-Name", clientConfig.clientName)
                        .addHeader("X-YouTube-Client-Version", clientConfig.clientVersion)
                        .addHeader("x-origin", "https://music.youtube.com")
                        .addHeader("User-Agent", clientConfig.userAgent)
                        .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    val playerResponse = json.decodeFromString(PlayerResponse.serializer(), responseBody)

                    if (playerResponse.playabilityStatus?.status == "OK") {
                        return@runCatching playerResponse
                    }

                    lastError = Exception(playerResponse.playabilityStatus?.reason ?: "Playability status not OK")
                } catch (e: Exception) {
                    lastError = e
                }
            }

            throw lastError ?: Exception("All clients failed")
        }
    }

    suspend fun getPipedStream(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://pipedapi.kavin.rocks/streams/$videoId")
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
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
        val content = tabs.firstOrNull()?.tabRenderer?.content ?: return songs
        val sections = content.sectionListRenderer?.contents ?: return songs

        for (section in sections) {
            val shelf = section.musicShelfRenderer ?: continue
            val items = shelf.contents ?: continue

            for (item in items) {
                val renderer = item.musicResponsiveListItemRenderer ?: continue
                val song = parseSongItem(renderer) ?: continue
                songs.add(song)
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
        val title = titleRun?.text ?: return null

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
