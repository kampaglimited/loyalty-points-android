package com.loyalty.android.repository

import android.content.Context
import kotlinx.coroutines.delay

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<String>
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveCredentials(username: String, password: String)
    fun getSavedUsername(): String?
    fun getSavedPassword(): String?
    fun clearCredentials()
}

class AuthRepositoryImpl(context: Context) : AuthRepository {
    private val prefs = context.getSharedPreferences("loyalty_prefs", Context.MODE_PRIVATE)

    override suspend fun login(username: String, password: String): Result<String> {
        // Mocked login logic with delay to show loading state
        delay(1000)
        return if (username == "admin" && password == "123456") {
            Result.success("mock_token_${System.currentTimeMillis()}")
        } else {
            Result.failure(Exception("Invalid credentials"))
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
