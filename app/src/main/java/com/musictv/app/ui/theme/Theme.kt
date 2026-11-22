package com.musictv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// ── Spotify-inspired Dark Color Palette ──────────────────────────────────────
val SpotifyGreen   = Color(0xFF1DB954)
val SpotifyBlack   = Color(0xFF0A0A0A)
val SpotifyDarkGrey = Color(0xFF121212)
val SpotifyGrey    = Color(0xFF282828)
val SpotifyLightGrey = Color(0xFF3E3E3E)
val SpotifyWhite   = Color(0xFFFFFFFF)
val SpotifySubText = Color(0xFFB3B3B3)
val CardBackground = Color(0xFF181818)
val PlayerBackground = Color(0xFF121212)
val AccentColor    = Color(0xFF1DB954)

private val DarkColorScheme = darkColorScheme(
    primary        = SpotifyGreen,
    onPrimary      = SpotifyBlack,
    secondary      = SpotifyLightGrey,
    onSecondary    = SpotifyWhite,
    background     = SpotifyBlack,
    onBackground   = SpotifyWhite,
    surface        = SpotifyDarkGrey,
    onSurface      = SpotifyWhite,
    surfaceVariant = SpotifyGrey,
    onSurfaceVariant = SpotifySubText,
    error          = Color(0xFFE53935),
    outline        = SpotifyLightGrey
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 36.sp,
        color      = SpotifyWhite,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        color      = SpotifyWhite
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        color      = SpotifyWhite
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        color      = SpotifyWhite
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        color      = SpotifyWhite
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        color      = SpotifySubText
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        color      = SpotifySubText
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        color      = SpotifySubText,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun YoutubeMusicTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
