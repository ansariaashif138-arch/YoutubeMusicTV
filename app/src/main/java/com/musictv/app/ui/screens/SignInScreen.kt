package com.musictv.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.musictv.app.ui.theme.*
import com.musictv.app.viewmodel.SignInState
import com.musictv.app.viewmodel.SignInViewModel

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    vm: SignInViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        if (state is SignInState.Success) onSignedIn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {
            is SignInState.Idle -> IdleSignIn(onStart = { vm.startSignIn() })
            is SignInState.Loading -> LoadingView()
            is SignInState.ShowCode -> DeviceCodeView(response = s.response)
            is SignInState.Success -> {} // navigated away
            is SignInState.Error -> ErrorView(message = s.message, onRetry = { vm.reset() })
        }
    }
}

@Composable
private fun IdleSignIn(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(48.dp)
    ) {
        // App logo placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SpotifyGreen),
            contentAlignment = Alignment.Center
        ) {
            Text("♫", fontSize = 56.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "YouTube Music TV",
            style = MaterialTheme.typography.headlineLarge,
            color = SpotifyWhite
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Spotify-style • Audio Only • Ad Free",
            style = MaterialTheme.typography.bodyLarge,
            color = SpotifySubText
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
            modifier = Modifier
                .height(56.dp)
                .width(280.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                "Sign in with Google",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SpotifyBlack
            )
        }
    }
}

@Composable
private fun DeviceCodeView(response: com.musictv.app.data.models.DeviceCodeResponse) {
    val qrBitmap = remember(response.verificationUrl) {
        generateQrCode("${response.verificationUrl}?user_code=${response.userCode}", 400)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Instructions
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign in on your phone",
                style = MaterialTheme.typography.headlineMedium,
                color = SpotifyWhite
            )
            Spacer(modifier = Modifier.height(16.dp))

            StepRow(step = "1", text = "Open your phone or computer")
            StepRow(step = "2", text = "Go to: ${response.verificationUrl}")
            StepRow(step = "3", text = "Enter the code below")

            Spacer(modifier = Modifier.height(32.dp))

            // The activation code — big and bold
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpotifyGrey)
                    .padding(horizontal = 32.dp, vertical = 20.dp)
            ) {
                Text(
                    text = response.userCode,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyGreen,
                    letterSpacing = 8.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Or scan the QR code →",
                color = SpotifySubText,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Right: QR Code
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SpotifyWhite)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun StepRow(step: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50))
                .background(SpotifyGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(step, color = SpotifyBlack, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = SpotifyWhite, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LoadingView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = SpotifyGreen, strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Connecting...", color = SpotifySubText)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚠️ $message", color = Color(0xFFE53935), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
        ) {
            Text("Try Again", color = SpotifyBlack)
        }
    }
}

private fun generateQrCode(content: String, size: Int): Bitmap? {
    return try {
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp  = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) { null }
}
