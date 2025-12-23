package com.loyalty.android.ui

sealed interface LoginStatus {
    data object Idle : LoginStatus
    data object Loading : LoginStatus
    data class Success(val token: String) : LoginStatus
    data class Error(val message: String, val attempts: Int) : LoginStatus
    data object LockedOut : LoginStatus
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoginEnabled: Boolean = false,
    val status: LoginStatus = LoginStatus.Idle,
    val rememberMe: Boolean = false,
    val isOnline: Boolean = true
)
