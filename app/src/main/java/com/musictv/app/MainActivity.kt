package com.musictv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.musictv.app.ui.screens.HomeScreen
import com.musictv.app.ui.screens.PlayerScreen
import com.musictv.app.ui.screens.PlaylistScreen
import com.musictv.app.ui.screens.SignInScreen
import com.musictv.app.ui.theme.YoutubeMusicTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YoutubeMusicTVTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "signin",
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        composable("signin") {
            SignInScreen(
                onSignedIn = { navController.navigate("home") { popUpTo("signin") { inclusive = true } } }
            )
        }
        composable("home") {
            HomeScreen(
                onPlaylistClick = { playlistId -> navController.navigate("playlist/$playlistId") },
                onTrackClick = { videoId -> navController.navigate("player/$videoId") }
            )
        }
        composable("playlist/{playlistId}") { backStack ->
            val playlistId = backStack.arguments?.getString("playlistId") ?: ""
            PlaylistScreen(
                playlistId = playlistId,
                onTrackClick = { videoId -> navController.navigate("player/$videoId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("player/{videoId}") { backStack ->
            val videoId = backStack.arguments?.getString("videoId") ?: ""
            PlayerScreen(
                videoId = videoId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
