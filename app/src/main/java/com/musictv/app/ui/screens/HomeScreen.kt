package com.musictv.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.musictv.app.data.models.Playlist
import com.musictv.app.data.models.Track
import com.musictv.app.data.repository.MusicRepository
import com.musictv.app.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.ui.platform.LocalContext

// ── ViewModel ──────────────────────────────────────────────────────────────────
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MusicRepository(application)

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _userName.value = repo.getUserName()
            _playlists.value = repo.getMyPlaylists()
            _isLoading.value = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    onPlaylistClick: (String) -> Unit,
    onTrackClick: (String) -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val playlists by vm.playlists.collectAsState()
    val userName  by vm.userName.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        // ── Sidebar ───────────────────────────────────────────────────────────
        Sidebar(userName = userName)

        // ── Main Content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
            // Greeting
            Text(
                text = "Good ${greeting()}, ${userName.substringBefore(" ")} 👋",
                style = MaterialTheme.typography.headlineLarge,
                color = SpotifyWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your Music • Your Vibe", color = SpotifySubText)

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            } else {
                // Quick access row (first 6 playlists)
                if (playlists.isNotEmpty()) {
                    Text(
                        "Quick Access",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SpotifyWhite
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(playlists.take(6)) { playlist ->
                            QuickAccessCard(playlist, onClick = { onPlaylistClick(playlist.id) })
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // All playlists
                Text(
                    "Your Library",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SpotifyWhite
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(playlists) { playlist ->
                        PlaylistCard(playlist, onClick = { onPlaylistClick(playlist.id) })
                    }
                }
            }
        }
    }
}

// ── Sidebar ───────────────────────────────────────────────────────────────────
@Composable
private fun Sidebar(userName: String) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(SpotifyDarkGrey)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SpotifyGreen),
                    contentAlignment = Alignment.Center
                ) { Text("♫", fontSize = 20.sp) }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Music TV", fontWeight = FontWeight.Bold, color = SpotifyWhite, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            SidebarItem(icon = Icons.Default.Home, label = "Home", selected = true)
            SidebarItem(icon = Icons.Default.LibraryMusic, label = "Library")
            SidebarItem(icon = Icons.Default.Favorite, label = "Liked Songs")
            SidebarItem(icon = Icons.Default.Search, label = "Search")
        }

        // User info at bottom
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SpotifyGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    userName.firstOrNull()?.uppercase() ?: "U",
                    color = SpotifyBlack,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                userName.substringBefore(" "),
                color = SpotifyWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SidebarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) SpotifyGrey else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) SpotifyGreen else SpotifySubText)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = if (selected) SpotifyWhite else SpotifySubText, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

// ── Cards ─────────────────────────────────────────────────────────────────────
@Composable
private fun QuickAccessCard(playlist: Playlist, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SpotifyGrey)
            .clickable(onClick = onClick)
    ) {
        if (playlist.isLikedSongs) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF4B0082), SpotifyGreen))),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Favorite, contentDescription = null, tint = SpotifyWhite) }
        } else {
            AsyncImage(
                model = playlist.thumbnailUrl,
                contentDescription = playlist.title,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = playlist.title,
            color = SpotifyWhite,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        if (playlist.isLikedSongs) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF4B0082), SpotifyGreen))),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Favorite, contentDescription = null, tint = SpotifyWhite, modifier = Modifier.size(48.dp)) }
        } else {
            AsyncImage(
                model = playlist.thumbnailUrl,
                contentDescription = playlist.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(playlist.title, color = SpotifyWhite, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (playlist.trackCount > 0) {
            Text("${playlist.trackCount} songs", color = SpotifySubText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun greeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        else -> "Evening"
    }
}
