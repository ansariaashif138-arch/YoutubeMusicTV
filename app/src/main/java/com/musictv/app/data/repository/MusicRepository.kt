package com.musictv.app.data.repository

import android.content.Context
import com.musictv.app.auth.AuthKeys
import com.musictv.app.auth.DeviceFlowAuth
import com.musictv.app.auth.dataStore
import com.musictv.app.data.api.InnertubeApi
import com.musictv.app.data.api.YouTubeApi
import com.musictv.app.data.models.AudioStream
import com.musictv.app.data.models.Playlist
import com.musictv.app.data.models.Track
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MusicRepository(private val context: Context) {

    private val auth         = DeviceFlowAuth(context)
    private val innertubeApi = InnertubeApi()

    private val youtubeApi: YouTubeApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApi::class.java)
    }

    private suspend fun bearer(): String {
        val token = auth.getAccessToken() ?: ""
        return "Bearer $token"
    }

    // ── Playlists ─────────────────────────────────────────────────────────────
    suspend fun getMyPlaylists(): List<Playlist> {
        return try {
            val response = youtubeApi.getMyPlaylists(bearer())
            val playlists = response.items.map { item ->
                Playlist(
                    id           = item.id,
                    title        = item.snippet.title,
                    description  = item.snippet.description,
                    thumbnailUrl = item.snippet.thumbnails.best(),
                    trackCount   = item.contentDetails?.itemCount ?: 0
                )
            }.toMutableList()

            // Add "Liked Songs" as first playlist
            playlists.add(0, Playlist(
                id           = "LL",
                title        = "Liked Songs",
                description  = "Songs you've liked",
                thumbnailUrl = "",
                trackCount   = 0,
                isLikedSongs = true
            ))
            playlists
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Tracks in a Playlist ──────────────────────────────────────────────────
    suspend fun getPlaylistTracks(playlistId: String): List<Track> {
        return try {
            val response = youtubeApi.getPlaylistItems(bearer(), playlistId = playlistId)
            response.items.map { item ->
                Track(
                    videoId      = item.snippet.resourceId.videoId,
                    title        = item.snippet.title,
                    artist       = item.snippet.videoOwnerChannelTitle ?: "Unknown Artist",
                    thumbnailUrl = item.snippet.thumbnails.best()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Audio Streams (Ad-free via Innertube) ─────────────────────────────────
    suspend fun getAudioStream(videoId: String): AudioStream? {
        val token = auth.getAccessToken() ?: return null
        val streams = innertubeApi.getAudioStreams(videoId, token)
        return streams.firstOrNull()
    }

    // ── User Info ─────────────────────────────────────────────────────────────
    suspend fun getUserName(): String =
        context.dataStore.data.first()[AuthKeys.USER_NAME] ?: "User"

    suspend fun getUserAvatar(): String =
        context.dataStore.data.first()[AuthKeys.USER_AVATAR] ?: ""
}
