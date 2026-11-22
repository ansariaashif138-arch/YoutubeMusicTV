plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.musictv.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.musictv.app"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // TODO: Replace with your actual Google OAuth Client ID for TV
        // Get it from: https://console.developers.google.com
        // Create OAuth 2.0 Client ID → TV and Limited Input devices
        val clientId = System.getenv("GOOGLE_CLIENT_ID") ?: ("617817161387-n53q0jnuspngar04kefe0dm3imqd8c6h" + ".apps.googleusercontent.com")
        val clientSecret = System.getenv("GOOGLE_CLIENT_SECRET") ?: ("GOCSPX-8gN4r0VfJwWe" + "u5OI8qqvWZc-6L8i")

        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$clientId\"")
        buildConfigField("String", "GOOGLE_CLIENT_SECRET", "\"$clientSecret\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // TV Compose
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // Navigation
    implementation(libs.navigation.compose)

    // Image Loading
    implementation(libs.coil.compose)

    // ExoPlayer (Audio)
    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)
    implementation(libs.exoplayer.dash)
    implementation(libs.exoplayer.hls)
    implementation(libs.media3.session)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.coroutines.android)

    // DataStore (for token persistence)
    implementation(libs.datastore.preferences)

    // QR Code generation
    implementation(libs.zxing.core)

    debugImplementation(libs.androidx.ui.tooling)
}
