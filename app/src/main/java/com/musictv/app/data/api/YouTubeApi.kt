package com.musictv.app.data.api

import com.musictv.app.data.models.YouTubePlaylistItemsResponse
import com.musictv.app.data.models.YouTubePlaylistsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface YouTubeApi {

    // Get user's playlists
    @GET("youtube/v3/playlists")
    suspend fun getMyPlaylists(
        @Header("Authorization") bearer: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 50
    ): YouTubePlaylistsResponse

    // Get tracks in a playlist
    @GET("youtube/v3/playlistItems")
    suspend fun getPlaylistItems(
        @Header("Authorization") bearer: String,
        @Query("part") part: String = "snippet",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null
    ): YouTubePlaylistItemsResponse

    // Get liked videos (special playlist: "LL")
    @GET("youtube/v3/playlistItems")
    suspend fun getLikedVideos(
        @Header("Authorization") bearer: String,
        @Query("part") part: String = "snippet",
        @Query("playlistId") playlistId: String = "LL",
        @Query("maxResults") maxResults: Int = 50
    ): YouTubePlaylistItemsResponse
}

interface GoogleAuthApi {
    // placeholder for auth API calls
}
