# YouTube Music TV 🎵

> Spotify-style Android TV app for YouTube Music — Audio Only, Ad-Free, Smooth UI

![Platform](https://img.shields.io/badge/Platform-Android%20TV-green)
![Language](https://img.shields.io/badge/Language-Kotlin-blue)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-orange)

---

## ✨ Features

| Feature | Detail |
|---------|--------|
| 🎨 **Spotify-style UI** | Dark elegant theme, smooth animations |
| 📺 **Android TV optimized** | Full D-pad & remote navigation |
| 🔑 **Google Sign-in** | QR code or TV remote (Device Flow) |
| 🎵 **Audio Only** | No video — music only via ExoPlayer |
| 🚫 **Ad-Free** | Direct audio streams via Innertube API |
| 📋 **Your Playlists** | View all your YouTube Music playlists |
| ❤️ **Liked Songs** | Access your liked songs playlist |

---

## 📱 Screenshots

| Sign In (QR Code) | Home | Player |
|:-:|:-:|:-:|
| TV screen shows QR + code | Spotify-style sidebar + playlist grid | Full-screen player with controls |

---

## 🛠️ Setup

### 1. Get Google OAuth Credentials

1. Go to [Google Cloud Console](https://console.developers.google.com)
2. Create a new project → **Enable YouTube Data API v3**
3. Create OAuth 2.0 credentials → **TV and Limited Input devices**
4. Copy your **Client ID** and **Client Secret**

### 2. Configure the App

Edit `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "GOOGLE_CLIENT_ID", "\"YOUR_CLIENT_ID.apps.googleusercontent.com\"")
buildConfigField("String", "GOOGLE_CLIENT_SECRET", "\"YOUR_CLIENT_SECRET\"")
```

### 3. Build the APK

```bash
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🏗️ Architecture

```
app/
├── auth/
│   └── DeviceFlowAuth.kt      # Google Device Flow OAuth
├── data/
│   ├── api/
│   │   ├── YouTubeApi.kt      # YouTube Data API v3 (Retrofit)
│   │   └── InnertubeApi.kt    # Audio stream extraction (ad-free)
│   ├── models/
│   │   └── Models.kt          # All data models
│   └── repository/
│       └── MusicRepository.kt # Single source of truth
├── player/
│   └── MusicPlaybackService.kt # ExoPlayer + MediaSession
├── ui/
│   ├── screens/
│   │   ├── SignInScreen.kt    # QR code sign-in screen
│   │   ├── HomeScreen.kt      # Spotify-style home + sidebar
│   │   ├── PlaylistScreen.kt  # Playlist track list
│   │   └── PlayerScreen.kt    # Full-screen player
│   └── theme/
│       └── Theme.kt           # Spotify color palette
└── viewmodel/
    └── SignInViewModel.kt     # Sign-in state management
```

---

## 🔐 GitHub Actions (Auto APK Build)

Add these secrets to your GitHub repo (**Settings → Secrets → Actions**):
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Every push to `main` will auto-build and upload the APK as an artifact.

---

## 📦 Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Compose for TV
- **Player**: ExoPlayer (Media3) — audio only
- **Auth**: Google OAuth 2.0 Device Flow
- **API**: YouTube Data API v3 + Innertube API
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Storage**: DataStore Preferences

---

## ⚠️ Disclaimer

This app is for personal use only. YouTube Music content is subject to YouTube's Terms of Service.

---

Made with ❤️ by [@ansariaashif138-arch](https://github.com/ansariaashif138-arch)
