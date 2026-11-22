package com.musictv.app.data.models

// ── Auth Models ───────────────────────────────────────────────────────────────
data class DeviceCodeResponse(
    val deviceCode: String,
    val userCode: String,
    val verificationUrl: String,
    val expiresIn: Int,
    val interval: Int
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String = "",
    val expiresIn: Int
)

// ── Music Models ──────────────────────────────────────────────────────────────
data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val trackCount: Int,
    val isLikedSongs: Boolean = false
)

data class Track(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val thumbnailUrl: String,
    val durationMs: Long = 0L
)

data class AudioStream(
    val url: String,
    val mimeType: String,
    val bitrate: Int,
    val quality: String
)

data class PlayerState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val durationMs: Long = 0L,
    val currentMs: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class HomeSection(
    val title: String,
    val playlists: List<Playlist>
)

// ── YouTube API Response Models ───────────────────────────────────────────────
data class YouTubePlaylistsResponse(
    val items: List<YouTubePlaylistItem> = emptyList(),
    val nextPageToken: String? = null
)

data class YouTubePlaylistItem(
    val id: String,
    val snippet: PlaylistSnippet,
    val contentDetails: PlaylistContentDetails?
)

data class PlaylistSnippet(
    val title: String,
    val description: String,
    val thumbnails: Thumbnails
)

data class PlaylistContentDetails(
    val itemCount: Int
)

data class Thumbnails(
    val high: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val default: Thumbnail? = null
) {
    fun best(): String = high?.url ?: medium?.url ?: default?.url ?: ""
}

data class Thumbnail(val url: String)

data class YouTubePlaylistItemsResponse(
    val items: List<YouTubePlaylistItemEntry> = emptyList(),
    val nextPageToken: String? = null
)

data class YouTubePlaylistItemEntry(
    val snippet: PlaylistItemSnippet
)

data class PlaylistItemSnippet(
    val title: String,
    val videoOwnerChannelTitle: String? = null,
    val thumbnails: Thumbnails,
    val resourceId: ResourceId
)

data class ResourceId(
    val videoId: String
)
