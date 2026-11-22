package com.musictv.app.ui.screens

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.musictv.app.data.models.Track
import com.musictv.app.data.repository.MusicRepository
import com.musictv.app.player.MusicPlaybackService
import com.musictv.app.ui.theme.*
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── ViewModel ──────────────────────────────────────────────────────────────────
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MusicRepository(application)

    private val _track     = MutableStateFlow<Track?>(null)
    val track: StateFlow<Track?> = _track.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress  = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error     = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var controller: MediaController? = null

    fun loadAndPlay(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null

            try {
                // Fetch audio stream
                val stream = repo.getAudioStream(videoId)
                if (stream == null) {
                    _error.value = "Could not load audio stream"
                    _isLoading.value = false
                    return@launch
                }

                // Set up track metadata (placeholder, enrich from playlist)
                _track.value = Track(
                    videoId      = videoId,
                    title        = "Loading...",
                    artist       = "YouTube Music",
                    thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                )

                // Play via MediaController
                val ctx  = getApplication<Application>()
                val token = SessionToken(ctx, ComponentName(ctx, MusicPlaybackService::class.java))
                val future = MediaController.Builder(ctx, token).buildAsync()

                future.addListener({
                    controller = future.get()
                    val mediaItem = MediaItem.Builder()
                        .setUri(stream.url)
                        .setMediaId(videoId)
                        .build()
                    controller?.setMediaItem(mediaItem)
                    controller?.prepare()
                    controller?.play()
                    _isPlaying.value = true
                    _isLoading.value = false
                }, MoreExecutors.directExecutor())

                // Progress updater
                while (true) {
                    delay(500)
                    val ctrl = controller ?: break
                    val dur  = ctrl.duration.takeIf { it > 0 } ?: break
                    _progress.value = ctrl.currentPosition.toFloat() / dur
                }

            } catch (e: Exception) {
                _error.value     = "Playback error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun togglePlay() {
        controller?.let {
            if (it.isPlaying) { it.pause(); _isPlaying.value = false }
            else              { it.play();  _isPlaying.value = true  }
        }
    }

    fun seekTo(fraction: Float) {
        controller?.let { ctrl ->
            val pos = (ctrl.duration * fraction).toLong()
            ctrl.seekTo(pos)
        }
    }

    override fun onCleared() {
        controller?.release()
        super.onCleared()
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun PlayerScreen(
    videoId: String,
    onBack: () -> Unit,
    vm: PlayerViewModel = viewModel()
) {
    val track     by vm.track.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val progress  by vm.progress.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error     by vm.error.collectAsState()

    LaunchedEffect(videoId) { vm.loadAndPlay(videoId) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBackground)
    ) {
        // Background blurred artwork
        track?.let { t ->
            AsyncImage(
                model = t.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )
        }

        // Dark overlay gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, SpotifyBlack)
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back", tint = SpotifyWhite, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("NOW PLAYING", color = SpotifySubText, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = SpotifySubText)
            }

            // ── Center: Album art + info ───────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Album art
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SpotifyGrey)
                ) {
                    track?.let { t ->
                        AsyncImage(
                            model = t.thumbnailUrl,
                            contentDescription = t.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SpotifyGreen)
                        }
                    }
                }

                // Track info + controls
                Column(modifier = Modifier.weight(1f)) {
                    track?.let { t ->
                        Text(t.title, style = MaterialTheme.typography.headlineMedium, color = SpotifyWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(t.artist, color = SpotifySubText, style = MaterialTheme.typography.titleMedium)
                    }

                    error?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("⚠️ $it", color = Color(0xFFE53935))
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // ── Progress bar ──────────────────────────────────────
                    Slider(
                        value = progress,
                        onValueChange = { vm.seekTo(it) },
                        colors = SliderDefaults.colors(
                            thumbColor       = SpotifyWhite,
                            activeTrackColor = SpotifyGreen,
                            inactiveTrackColor = SpotifyLightGrey
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Playback controls ─────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = SpotifySubText, modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = SpotifyWhite, modifier = Modifier.size(40.dp))
                        }

                        // Play/Pause — big green circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                                .clickable { vm.togglePlay() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = SpotifyBlack,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        IconButton(onClick = {}) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = SpotifyWhite, modifier = Modifier.size(40.dp))
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = SpotifySubText, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Modifier.clickable(onClick: () -> Unit) =
    this.then(Modifier.clickable { onClick() })
