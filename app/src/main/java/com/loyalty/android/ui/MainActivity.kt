package com.loyalty.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.loyalty.android.repository.AuthRepositoryImpl
import com.loyalty.android.util.NetworkMonitorImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel = LoginViewModel(
            repository = AuthRepositoryImpl(applicationContext),
            networkMonitor = NetworkMonitorImpl(applicationContext)
        )
        
        setContent {
            LoginScreen(viewModel = viewModel)
        }
    }
}
