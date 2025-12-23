package com.loyalty.android.domain

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Invalid(
        val usernameError: String? = null,
        val passwordError: String? = null
    ) : ValidationResult()
}

class LoginValidator {
    /**
     * Validates that the username is not empty and the password 
     * is at least 6 characters long (after trimming whitespace).
     */
    fun validateCredentials(username: String, password: String): ValidationResult {
        val isUsernameValid = username.trim().isNotEmpty()
        val isPasswordValid = password.trim().length >= 6
        
        return if (isUsernameValid && isPasswordValid) {
            ValidationResult.Success
        } else {
            ValidationResult.Invalid(
                usernameError = if (isUsernameValid) null else "Username cannot be empty",
                passwordError = if (isPasswordValid) null else "Password must be at least 6 characters"
            )
        }
    }
}
