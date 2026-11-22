package com.musictv.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musictv.app.auth.DeviceFlowAuth
import com.musictv.app.data.models.DeviceCodeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignInState {
    object Idle : SignInState()
    object Loading : SignInState()
    data class ShowCode(val response: DeviceCodeResponse) : SignInState()
    object Success : SignInState()
    data class Error(val message: String) : SignInState()
}

class SignInViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = DeviceFlowAuth(application)

    private val _state = MutableStateFlow<SignInState>(SignInState.Idle)
    val state: StateFlow<SignInState> = _state.asStateFlow()

    fun startSignIn() {
        viewModelScope.launch {
            _state.value = SignInState.Loading
            try {
                val deviceResponse = auth.requestDeviceCode()
                _state.value = SignInState.ShowCode(deviceResponse)

                // Poll in background
                val token = auth.pollForToken(
                    deviceCode  = deviceResponse.deviceCode,
                    intervalSec = deviceResponse.interval
                )

                _state.value = if (token != null) SignInState.Success
                               else SignInState.Error("Sign in timed out. Please try again.")
            } catch (e: Exception) {
                _state.value = SignInState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun reset() { _state.value = SignInState.Idle }
}
