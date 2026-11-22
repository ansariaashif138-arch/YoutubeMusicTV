package com.musictv.app.ui.screens

import android.app.Application
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.musictv.app.data.models.Track
import com.musictv.app.data.repository.MusicRepository
import com.musictv.app.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ──────────────────────────────────────────────────────────────────
class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MusicRepository(application)

    private val _tracks    = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _tracks.value    = repo.getPlaylistTracks(playlistId)
            _isLoading.value = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun PlaylistScreen(
    playlistId: String,
    onTrackClick: (String) -> Unit,
    onBack: () -> Unit,
    vm: PlaylistViewModel = viewModel()
) {
    val tracks    by vm.tracks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    LaunchedEffect(playlistId) { vm.loadPlaylist(playlistId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A472A), SpotifyBlack)
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SpotifyWhite)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Playlist", color = SpotifySubText, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (playlistId == "LL") "Liked Songs" else "Your Playlist",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SpotifyWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${tracks.size} songs", color = SpotifySubText)
                }
            }
        }

        // ── Play All Button ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = { tracks.firstOrNull()?.let { onTrackClick(it.videoId) } },
                containerColor = SpotifyGreen,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play All", tint = SpotifyBlack)
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = {}) {
                Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = SpotifySubText)
            }
        }

        // ── Track List ────────────────────────────────────────────────────────
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpotifyGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(tracks) { index, track ->
                    TrackRow(
                        index   = index + 1,
                        track   = track,
                        onClick = { onTrackClick(track.videoId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(index: Int, track: Track, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (focused) SpotifyGrey else Color.Transparent)
            .clickable(onClick = onClick)
            .onFocusChanged { focused = it.isFocused }
            .padding(12.dp)
    ) {
        // Track number
        Text(
            text = "$index",
            color = SpotifySubText,
            modifier = Modifier.width(32.dp)
        )

        // Thumbnail
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = track.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = SpotifyWhite,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = SpotifySubText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Play icon on focus
        if (focused) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SpotifyGreen)
        }
    }
}

@Composable
fun Modifier.onFocusChanged(block: (androidx.compose.ui.focus.FocusState) -> Unit): Modifier =
    this.then(Modifier.onFocusChanged(block))
