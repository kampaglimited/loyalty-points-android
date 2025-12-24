package com.loyalty.android.repository

import android.content.Context
import kotlinx.coroutines.delay

sealed class AuthResult {
    data class Success(val token: String) : AuthResult()
    data class InvalidCredentials(val message: String) : AuthResult()
    data class NetworkError(val message: String) : AuthResult()
    data object AccountLocked : AuthResult()
}

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthResult
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveCredentials(username: String, password: String)
    fun getSavedUsername(): String?
    fun getSavedPassword(): String?
    fun clearCredentials()
}

class AuthRepositoryImpl(context: Context) : AuthRepository {
    private val prefs = context.getSharedPreferences("loyalty_prefs", Context.MODE_PRIVATE)

    override suspend fun login(username: String, password: String): AuthResult {
        // NOTE: This is a mocked authentication implementation for demonstration purposes.
        // In a production environment, this would perform a secure network request to an auth server.
        // The following credentials are provided for testing and demo flows.
        val testAccounts = mapOf(
            "admin" to "123456",
            "user1" to "password123",
            "tester" to "testpass"
        )

        // Mocked login logic with delay to show loading state
        delay(1000)

        val expectedPassword = testAccounts[username]
        return if (expectedPassword != null) {
            if (expectedPassword == password) {
                AuthResult.Success("mock_token_${System.currentTimeMillis()}")
            } else {
                AuthResult.InvalidCredentials("Incorrect password")
            }
        } else {
            AuthResult.InvalidCredentials("User not found")
        }
    }

    override fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    override fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    override fun saveCredentials(username: String, password: String) {
        prefs.edit().apply {
            putString("saved_username", username)
            putString("saved_password", password)
            putBoolean("remember_me", true)
        }.apply()
    }

    override fun getSavedUsername(): String? = prefs.getString("saved_username", null)
    override fun getSavedPassword(): String? = prefs.getString("saved_password", null)
    
    override fun clearCredentials() {
        prefs.edit().apply {
            remove("saved_username")
            remove("saved_password")
            putBoolean("remember_me", false)
        }.apply()
    }
}
