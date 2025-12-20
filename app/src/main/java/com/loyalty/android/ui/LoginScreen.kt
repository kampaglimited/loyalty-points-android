package com.loyalty.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.navigateToHome) {
        HomeView(
            token = uiState.errorMessage ?: "No Token Found",
            isOnline = uiState.isOnline,
            onLogout = { viewModel.onLogoutClicked() }
        )
    } else {
        LoginContent(uiState, viewModel)
    }
}

@Composable
fun LoginContent(uiState: LoginUiState, viewModel: LoginViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loyalty Points",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TextField(
            value = uiState.username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text("Username") },
            enabled = !uiState.isLoading && !uiState.isLockedOut,
            modifier = Modifier.fillMaxWidth().testTag("username_field")
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Password") },
            enabled = !uiState.isLoading && !uiState.isLockedOut,
            modifier = Modifier.fillMaxWidth().testTag("password_field")
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.rememberMe,
                onCheckedChange = { viewModel.onRememberMeChanged(it) },
                modifier = Modifier.testTag("remember_me_checkbox")
            )
            Text("Remember Me")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.onLoginClicked() },
            enabled = uiState.isLoginEnabled && !uiState.isLoading && !uiState.isLockedOut,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_button")
        ) {
            Text(if (uiState.isLoading) "Logging in..." else "Login")
        }

        if (uiState.errorMessage != null && !uiState.navigateToHome) {
            Text(
                text = uiState.errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp).testTag("error_message")
            )
        }

        if (uiState.isLockedOut) {
            Text(
                text = "Contact support to unlock your account.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun HomeView(token: String, isOnline: Boolean, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().testTag("home_view")
    ) {
        if (!isOnline) {
            Surface(
                color = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth().height(32.dp).testTag("offline_banner")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Offline Mode - Data may be stale", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✓",
                color = Color.Green,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Login Successful!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Welcome to your dashboard.",
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth().testTag("token_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Persistence Check (Remember Me):", style = MaterialTheme.typography.labelLarge)
                    Text(token, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().testTag("logout_button")
            ) {
                Text("Logout & Test Autofill")
            }
        }
    }
}
