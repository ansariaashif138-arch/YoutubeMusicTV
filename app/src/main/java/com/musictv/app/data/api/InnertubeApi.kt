package com.musictv.app.data.api

import com.musictv.app.data.models.AudioStream
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Uses YouTube's internal Innertube API to extract audio-only streams.
 * This is the same API used by YouTube apps internally — no ads in direct streams.
 */
class InnertubeApi {

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent",    INNERTUBE_USER_AGENT)
                .header("Content-Type",  "application/json")
                .header("X-YouTube-Client-Name",    "67") // TV client
                .header("X-YouTube-Client-Version", "2.0")
                .build()
            chain.proceed(request)
        }
        .build()

    /**
     * Fetch audio-only stream URLs for a given YouTube videoId.
     * Returns sorted list of audio streams (best quality first).
     */
    suspend fun getAudioStreams(videoId: String, accessToken: String): List<AudioStream> {
        val body = buildPlayerRequest(videoId)

        val request = Request.Builder()
            .url("$INNERTUBE_BASE/youtubei/v1/player?key=$INNERTUBE_KEY")
            .post(okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("application/json"),
                body
            ))
            .header("Authorization", "Bearer $accessToken")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val json     = JSONObject(response.body!!.string())
            parseAudioStreams(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseAudioStreams(json: JSONObject): List<AudioStream> {
        val streams   = mutableListOf<AudioStream>()
        val formats   = json
            .optJSONObject("streamingData")
            ?.optJSONArray("adaptiveFormats") ?: return emptyList()

        for (i in 0 until formats.length()) {
            val fmt      = formats.getJSONObject(i)
            val mimeType = fmt.optString("mimeType", "")

            // Audio only streams
            if (!mimeType.startsWith("audio/")) continue

            val url     = fmt.optString("url").takeIf { it.isNotEmpty() } ?: continue
            val bitrate = fmt.optInt("bitrate", 0)
            val quality = fmt.optString("audioQuality", "UNKNOWN")

            streams.add(AudioStream(
                url      = url,
                mimeType = mimeType,
                bitrate  = bitrate,
                quality  = quality
            ))
        }

        // Best quality first (highest bitrate)
        return streams.sortedByDescending { it.bitrate }
    }

    private fun buildPlayerRequest(videoId: String): String {
        return """
        {
          "context": {
            "client": {
              "clientName": "TVHTML5",
              "clientVersion": "2.0",
              "hl": "en",
              "gl": "US",
              "deviceMake": "Generic",
              "deviceModel": "Generic",
              "platform": "TV"
            }
          },
          "videoId": "$videoId",
          "playbackContext": {
            "contentPlaybackContext": {
              "signatureTimestamp": 0,
              "referer": "https://www.youtube.com/"
            }
          },
          "racyCheckOk": false,
          "contentCheckOk": false
        }
        """.trimIndent()
    }

    companion object {
        private const val INNERTUBE_BASE       = "https://www.youtube.com"
        private const val INNERTUBE_KEY        = "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8"
        private const val INNERTUBE_USER_AGENT =
            "Mozilla/5.0 (SMART-TV; Linux; Tizen 6.0) AppleWebKit/538.1 (KHTML, like Gecko) Version/6.0 TV Safari/538.1"
    }
}
