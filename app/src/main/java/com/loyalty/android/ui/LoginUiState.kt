package com.loyalty.android.ui

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoginEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isLockedOut: Boolean = false,
    val errorMessage: String? = null,
    val navigateToHome: Boolean = false,
    val rememberMe: Boolean = false,
    val isOnline: Boolean = true
)
