package com.loyalty.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalty.android.domain.LoginValidator
import com.loyalty.android.domain.ValidationResult
import com.loyalty.android.repository.AuthRepository
import com.loyalty.android.repository.AuthResult
import com.loyalty.android.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val networkMonitor: NetworkMonitor,
    private val loginValidator: LoginValidator
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
                    val updatedStatus = if (!isOnline && state.status !is LoginStatus.Success) {
                        LoginStatus.Error("Network unavailable", loginAttempts)
                    } else if (isOnline && state.status is LoginStatus.Error && (state.status as LoginStatus.Error).message == "Network unavailable") {
                        LoginStatus.Idle
                    } else {
                        state.status
                    }
                    
                    state.copy(
                        isOnline = isOnline,
                        status = updatedStatus
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
            status = if (!isOnline) LoginStatus.Error("Network unavailable", loginAttempts) else LoginStatus.Idle
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
        val validationResult = loginValidator.validateCredentials(state.username, state.password)
        
        val isValid = validationResult is ValidationResult.Success && 
                      state.isOnline && 
                      state.status !is LoginStatus.Loading && 
                      state.status !is LoginStatus.LockedOut
        
        _uiState.update { it.copy(isLoginEnabled = isValid) }
    }

    fun onLoginClicked() {
        if (_uiState.value.status is LoginStatus.LockedOut) return

        viewModelScope.launch {
            val isOnline = networkMonitor.isOnline.first()
            if (!isOnline) {
                _uiState.update { it.copy(status = LoginStatus.Error("Network unavailable", loginAttempts)) }
                return@launch
            }

            _uiState.update { it.copy(status = LoginStatus.Loading) }
            validate()
            
            val trimmedUser = _uiState.value.username.trim()
            val trimmedPass = _uiState.value.password.trim()
            val result = repository.login(trimmedUser, trimmedPass)
            
            when (result) {
                is AuthResult.Success -> {
                    loginAttempts = 0
                    val token = result.token
                    if (_uiState.value.rememberMe) {
                        repository.saveToken(token)
                        repository.saveCredentials(_uiState.value.username, _uiState.value.password)
                    } else {
                        repository.clearCredentials()
                    }
                    _uiState.update { it.copy(
                        status = LoginStatus.Success("Saved Token: ${repository.getToken() ?: "None"}")
                    ) }
                    validate()
                }
                is AuthResult.InvalidCredentials -> {
                    loginAttempts++
                    val isLocked = loginAttempts >= MAX_ATTEMPTS
                    val message = if (isLocked) "Account locked" else "${result.message}. Attempt $loginAttempts/$MAX_ATTEMPTS"
                    
                    _uiState.update { it.copy(
                        status = if (isLocked) LoginStatus.LockedOut else LoginStatus.Error(message, loginAttempts)
                    ) }
                    validate()
                }
                is AuthResult.NetworkError -> {
                    _uiState.update { it.copy(
                        status = LoginStatus.Error(result.message, loginAttempts)
                    ) }
                    validate()
                }
                is AuthResult.AccountLocked -> {
                    _uiState.update { it.copy(status = LoginStatus.LockedOut) }
                    validate()
                }
            }
        }
    }
}
