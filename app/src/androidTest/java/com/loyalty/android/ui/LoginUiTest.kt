package com.loyalty.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.loyalty.android.repository.AuthRepository
import com.loyalty.android.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class LoginUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    private lateinit var repository: AuthRepository
    private lateinit var networkMonitor: NetworkMonitor
    private val isOnlineFlow = MutableStateFlow(true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        repository = mock(AuthRepository::class.java)
        networkMonitor = mock(NetworkMonitor::class.java)
        `when`(networkMonitor.isOnline).thenReturn(isOnlineFlow)
        `when`(repository.getSavedUsername()).thenReturn(null)
        
        viewModel = LoginViewModel(repository, networkMonitor)
    }

    private fun setContentWithStabilization() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.activityRule.scenario.state == Lifecycle.State.RESUMED
        }
        Thread.sleep(1000)
        composeTestRule.activity.runOnUiThread {
            composeTestRule.setContent {
                LoginScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testLoginButtonDisabledUntilValid() {
        setContentWithStabilization()
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("username_field").performTextInput("admin")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()
    }

    @Test
    fun testUsernameTrimming() {
        setContentWithStabilization()
        // Spaces shouldn't enable the button
        composeTestRule.onNodeWithTag("username_field").performTextInput("   ")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()

        // Valid username after trim
        composeTestRule.onNodeWithTag("username_field").performTextReplacement("admin")
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()
    }

    @Test
    fun testOfflineDisablesLoginButton() {
        setContentWithStabilization()
        composeTestRule.onNodeWithTag("username_field").performTextInput("admin")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()

        isOnlineFlow.value = false
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()

        isOnlineFlow.value = true
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()
    }

    @Test
    fun testFailedLoginShowsError() {
        runBlocking {
            `when`(repository.login(anyString(), anyString())).thenReturn(Result.failure(Exception("Invalid")))
        }
        setContentWithStabilization()

        composeTestRule.onNodeWithTag("username_field").performTextInput("admin")
        composeTestRule.onNodeWithTag("password_field").performTextInput("wrongpass")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Invalid credentials. Attempt 1/3").assertIsDisplayed()
    }

    @Test
    fun testLockoutFlow() {
        runBlocking {
            `when`(repository.login(anyString(), anyString())).thenReturn(Result.failure(Exception("Failed")))
        }
        setContentWithStabilization()

        composeTestRule.onNodeWithTag("username_field").performTextInput("admin")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")

        // 3 Failed attempts
        repeat(3) {
            composeTestRule.onNodeWithTag("login_button").performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule.onNodeWithText("Account locked").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("username_field").assertIsNotEnabled()
    }

    @Test
    fun testSuccessfulLoginFlow() {
        runBlocking {
            `when`(repository.login("admin", "123456")).thenReturn(Result.success("test-token"))
        }
        setContentWithStabilization()

        composeTestRule.onNodeWithTag("username_field").performTextInput("admin")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.onNodeWithTag("home_view").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login Successful!").assertIsDisplayed()
    }

    @Test
    fun testOfflineBannerVisibility() {
        runBlocking {
            `when`(repository.login("admin", "123456")).thenReturn(Result.success("token"))
        }
        setContentWithStabilization()

        composeTestRule.onNodeWithTag("username_field").performTextInput("admin")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        composeTestRule.onNodeWithTag("login_button").performClick()

        isOnlineFlow.value = false
        composeTestRule.onNodeWithTag("offline_banner").assertIsDisplayed()
    }

    @Test
    fun testLogoutReturnsToLogin() {
        runBlocking {
            `when`(repository.login("admin", "123456")).thenReturn(Result.success("token"))
        }
        setContentWithStabilization()

        composeTestRule.onNodeWithTag("username_field").performTextInput("admin")
        composeTestRule.onNodeWithTag("password_field").performTextInput("123456")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.onNodeWithTag("logout_button").performClick()

        composeTestRule.onNodeWithTag("username_field").assertIsDisplayed()
    }
}
