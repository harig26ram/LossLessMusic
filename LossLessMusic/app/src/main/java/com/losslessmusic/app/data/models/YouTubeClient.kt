package com.losslessmusic.app.data.models

data class YouTubeClient(
    val clientName: String,
    val clientVersion: String,
    val apiKey: String,
    val userAgent: String,
    val referer: String? = null,
    val osVersion: String? = null,
) {
    companion object {
        const val BASE_URL = "https://music.youtube.com/youtubei/v1/"
        const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"

        val WEB_REMIX = YouTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20240617.01.00",
            apiKey = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            referer = "https://music.youtube.com/",
        )

        val ANDROID_MUSIC = YouTubeClient(
            clientName = "ANDROID_MUSIC",
            clientVersion = "5.01",
            apiKey = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI",
            userAgent = "com.google.android.apps.youtube.music/5.01 (Linux; U; Android 14; en_US; 13R Build/AP2A.240503.002)",
            referer = "https://music.youtube.com/",
            osVersion = "14",
        )

        val IOS = YouTubeClient(
            clientName = "IOS",
            clientVersion = "19.29.1",
            apiKey = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc",
            userAgent = "com.google.ios.youtube/19.29.1 (iPhone15,3; U; CPU iOS 17_5 like Mac OS X)",
            referer = "https://music.youtube.com/",
            osVersion = "17.5",
        )

        val TVHTML5 = YouTubeClient(
            clientName = "TVHTML5",
            clientVersion = "7.20240612.00.00",
            apiKey = "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8",
            userAgent = "Mozilla/5.0 (SMART-TV; Linux; Tizen 7.0) AppleWebKit/537.36",
            referer = "https://music.youtube.com/",
            osVersion = null,
        )

        val ANDROID = YouTubeClient(
            clientName = "ANDROID",
            clientVersion = "19.29.38",
            apiKey = "AIzaSyA8eiYZq_TqUXFXVhgy6G_5_QdUExKqK4U",
            userAgent = "com.google.android.youtube/19.29.38 (Linux; U; Android 14) gzip",
            referer = "https://www.youtube.com/",
            osVersion = "14",
        )
    }

    fun toContext(locale: YouTubeLocale, visitorData: String): Context {
        return Context(
            client = Client(
                clientName = clientName,
                clientVersion = clientVersion,
                osVersion = osVersion ?: "14",
                userAgent = userAgent,
            ),
            user = User(),
            request = Request()
        )
    }
}
