package com.loyalty.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalty.android.repository.AuthRepository
import com.loyalty.android.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private var loginAttempts = 0
    private val MAX_ATTEMPTS = 3

    init {
        // Pre-load saved credentials if they exist
        val savedUser = repository.getSavedUsername()
        val savedPass = repository.getSavedPassword()
        if (savedUser != null) {
            _uiState.update { it.copy(
                username = savedUser, 
                password = savedPass ?: "",
                rememberMe = true
            ) }
            validate()
        }

        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { state ->
                    state.copy(
                        isOnline = isOnline,
                        errorMessage = if (!isOnline && !state.navigateToHome) "Network unavailable" else if (isOnline && state.errorMessage == "Network unavailable") null else state.errorMessage
                    )
                }
                validate()
            }
        }
    }

    fun onLogoutClicked() {
        val savedUser = repository.getSavedUsername()
        val savedPass = repository.getSavedPassword()
        val isOnline = _uiState.value.isOnline
        
        _uiState.update { it.copy(
            username = savedUser ?: "",
            password = savedPass ?: "",
            rememberMe = savedUser != null,
            navigateToHome = false, 
            errorMessage = if (!isOnline) "Network unavailable" else null,
            isLoading = false
        ) }
        validate()
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
        validate()
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
        validate()
    }

    fun onRememberMeChanged(remember: Boolean) {
        _uiState.update { it.copy(rememberMe = remember) }
    }

    private fun validate() {
        val state = _uiState.value
        val isValid = state.username.trim().isNotEmpty() && 
                      state.password.trim().length >= 6 &&
                      state.isOnline && 
                      !state.isLoading && 
                      !state.isLockedOut
        
        _uiState.update { it.copy(isLoginEnabled = isValid) }
    }

    fun onLoginClicked() {
        if (_uiState.value.isLockedOut) return

        viewModelScope.launch {
            val isOnline = networkMonitor.isOnline.first()
            if (!isOnline) {
                _uiState.update { it.copy(errorMessage = "Network unavailable") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            validate()
            
            val trimmedUser = _uiState.value.username.trim()
            val trimmedPass = _uiState.value.password.trim()
            val result = repository.login(trimmedUser, trimmedPass)
            // ...
            
            if (result.isSuccess) {
                loginAttempts = 0
                val token = result.getOrThrow()
                if (_uiState.value.rememberMe) {
                    repository.saveToken(token)
                    repository.saveCredentials(_uiState.value.username, _uiState.value.password)
                } else {
                    repository.clearCredentials()
                }
                _uiState.update { it.copy(
                    isLoading = false, 
                    navigateToHome = true,
                    errorMessage = "Saved Token: ${repository.getToken() ?: "None"}"
                ) }
                validate()
            } else {
                loginAttempts++
                val isLocked = loginAttempts >= MAX_ATTEMPTS
                _uiState.update { it.copy(
                    isLoading = false,
                    isLockedOut = isLocked,
                    errorMessage = if (isLocked) "Account locked" else "Invalid credentials. Attempt $loginAttempts/$MAX_ATTEMPTS"
                ) }
                validate()
            }
        }
    }
}
