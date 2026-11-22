package com.musictv.app.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.musictv.app.BuildConfig
import com.musictv.app.data.api.GoogleAuthApi
import com.musictv.app.data.models.DeviceCodeResponse
import com.musictv.app.data.models.TokenResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

object AuthKeys {
    val ACCESS_TOKEN  = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USER_EMAIL    = stringPreferencesKey("user_email")
    val USER_NAME     = stringPreferencesKey("user_name")
    val USER_AVATAR   = stringPreferencesKey("user_avatar")
}

class DeviceFlowAuth(private val context: Context) {

    private val client = OkHttpClient()

    // ── Step 1: Request device & user codes ──────────────────────────────────
    suspend fun requestDeviceCode(): DeviceCodeResponse {
        val body = FormBody.Builder()
            .add("client_id", BuildConfig.GOOGLE_CLIENT_ID)
            .add("scope", SCOPES)
            .build()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/device/code")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val json     = JSONObject(response.body!!.string())

        return DeviceCodeResponse(
            deviceCode      = json.getString("device_code"),
            userCode        = json.getString("user_code"),
            verificationUrl = json.getString("verification_url"),
            expiresIn       = json.getInt("expires_in"),
            interval        = json.getInt("interval")
        )
    }

    // ── Step 2: Poll for token ────────────────────────────────────────────────
    suspend fun pollForToken(deviceCode: String, intervalSec: Int): TokenResponse? {
        val maxWait = 300_000L // 5 minutes
        val start   = System.currentTimeMillis()

        while (System.currentTimeMillis() - start < maxWait) {
            delay(intervalSec * 1000L)

            val body = FormBody.Builder()
                .add("client_id",     BuildConfig.GOOGLE_CLIENT_ID)
                .add("client_secret", BuildConfig.GOOGLE_CLIENT_SECRET)
                .add("device_code",   deviceCode)
                .add("grant_type",    "urn:ietf:params:oauth2:grant-type:device_code")
                .build()

            val request = Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val json     = JSONObject(response.body!!.string())

            when {
                json.has("access_token") -> {
                    val token = TokenResponse(
                        accessToken  = json.getString("access_token"),
                        refreshToken = json.optString("refresh_token"),
                        expiresIn    = json.getInt("expires_in")
                    )
                    saveToken(token)
                    fetchAndSaveUserInfo(token.accessToken)
                    return token
                }
                json.optString("error") == "authorization_pending" -> continue
                json.optString("error") == "slow_down" -> delay(5000)
                else -> return null
            }
        }
        return null
    }

    // ── Refresh Access Token ──────────────────────────────────────────────────
    suspend fun refreshToken(): Boolean {
        val refreshToken = context.dataStore.data.first()[AuthKeys.REFRESH_TOKEN] ?: return false

        val body = FormBody.Builder()
            .add("client_id",     BuildConfig.GOOGLE_CLIENT_ID)
            .add("client_secret", BuildConfig.GOOGLE_CLIENT_SECRET)
            .add("refresh_token", refreshToken)
            .add("grant_type",    "refresh_token")
            .build()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val json     = JSONObject(response.body!!.string())

        return if (json.has("access_token")) {
            context.dataStore.edit { prefs ->
                prefs[AuthKeys.ACCESS_TOKEN] = json.getString("access_token")
            }
            true
        } else false
    }

    // ── Fetch User Profile ────────────────────────────────────────────────────
    private suspend fun fetchAndSaveUserInfo(accessToken: String) {
        val request = Request.Builder()
            .url("https://www.googleapis.com/oauth2/v2/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = client.newCall(request).execute()
        val json     = JSONObject(response.body!!.string())

        context.dataStore.edit { prefs ->
            prefs[AuthKeys.USER_EMAIL]  = json.optString("email")
            prefs[AuthKeys.USER_NAME]   = json.optString("name")
            prefs[AuthKeys.USER_AVATAR] = json.optString("picture")
        }
    }

    // ── Token Storage ─────────────────────────────────────────────────────────
    private suspend fun saveToken(token: TokenResponse) {
        context.dataStore.edit { prefs ->
            prefs[AuthKeys.ACCESS_TOKEN]  = token.accessToken
            if (token.refreshToken.isNotEmpty()) {
                prefs[AuthKeys.REFRESH_TOKEN] = token.refreshToken
            }
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.first()[AuthKeys.ACCESS_TOKEN]

    fun isLoggedIn(): Flow<Boolean> =
        context.dataStore.data.map { it[AuthKeys.ACCESS_TOKEN] != null }

    suspend fun signOut() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        const val SCOPES =
            "https://www.googleapis.com/auth/youtube.readonly " +
            "https://www.googleapis.com/auth/userinfo.profile " +
            "https://www.googleapis.com/auth/userinfo.email"
    }
}
