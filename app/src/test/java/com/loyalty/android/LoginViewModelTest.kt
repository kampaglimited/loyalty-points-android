package com.loyalty.android

import com.loyalty.android.repository.AuthRepository
import com.loyalty.android.repository.AuthResult
import com.loyalty.android.ui.LoginStatus
import com.loyalty.android.ui.LoginViewModel
import com.loyalty.android.domain.LoginValidator
import com.loyalty.android.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var repository: AuthRepository
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var loginValidator: LoginValidator
    private val isOnlineFlow = MutableStateFlow(true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(AuthRepository::class.java)
        networkMonitor = mock(NetworkMonitor::class.java)
        loginValidator = LoginValidator()
        `when`(networkMonitor.isOnline).thenReturn(isOnlineFlow)
        
        // Default init
        viewModel = LoginViewModel(repository, networkMonitor, loginValidator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Validation enables-disables button`() {
        // Initially disabled
        assertFalse(viewModel.uiState.value.isLoginEnabled)

        // Only username -> still disabled
        viewModel.onUsernameChanged("admin")
        assertFalse(viewModel.uiState.value.isLoginEnabled)

        // Valid username and too short password -> disabled
        viewModel.onPasswordChanged("12345")
        assertFalse(viewModel.uiState.value.isLoginEnabled)

        // Valid username and valid password -> enabled
        viewModel.onPasswordChanged("123456")
        assertTrue(viewModel.uiState.value.isLoginEnabled)
    }

    @Test
    fun `Validation trims whitespace`() {
        viewModel.onUsernameChanged("   ")
        viewModel.onPasswordChanged("123456")
        assertFalse("Spaces only username should be disabled", viewModel.uiState.value.isLoginEnabled)

        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("   123   ")
        assertFalse("Whitespace password below 6 chars should be disabled", viewModel.uiState.value.isLoginEnabled)
        
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged(" 123456 ")
        assertTrue("Password with leading-trailing spaces but valid length should match", viewModel.uiState.value.isLoginEnabled)
    }

    @Test
    fun `Validation disables when offline`() {
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("123456")
        assertTrue(viewModel.uiState.value.isLoginEnabled)

        // Go offline
        isOnlineFlow.value = false
        assertFalse("Should be disabled when offline", viewModel.uiState.value.isLoginEnabled)
        
        // Go back online
        isOnlineFlow.value = true
        assertTrue("Should re-enable when online", viewModel.uiState.value.isLoginEnabled)
    }

    @Test
    fun `Pre-loads saved credentials on init`() {
        `when`(repository.getSavedUsername()).thenReturn("saved_user")
        `when`(repository.getSavedPassword()).thenReturn("saved_pass")
        
        // Re-init to trigger pre-load
        val newViewModel = LoginViewModel(repository, networkMonitor, loginValidator)
        
        assertEquals("saved_user", newViewModel.uiState.value.username)
        assertEquals("saved_pass", newViewModel.uiState.value.password)
        assertTrue(newViewModel.uiState.value.rememberMe)
    }

    @Test
    fun `Reactive network banner updates immediately`() = runTest {
        // Go offline
        isOnlineFlow.value = false
        val status = viewModel.uiState.value.status
        assertTrue(status is LoginStatus.Error)
        assertEquals("Network unavailable", (status as LoginStatus.Error).message)
        assertFalse(viewModel.uiState.value.isOnline)

        // Go online
        isOnlineFlow.value = true
        assertTrue(viewModel.uiState.value.status is LoginStatus.Idle)
        assertTrue(viewModel.uiState.value.isOnline)
    }

    @Test
    fun `Logout resets navigation and syncs with storage`() = runTest {
        // Simulate logged in state
        viewModel.onUsernameChanged("temp_user")
        `when`(repository.login(anyString(), anyString())).thenReturn(AuthResult.Success("token"))
        viewModel.onLoginClicked()
        assertTrue(viewModel.uiState.value.status is LoginStatus.Success)

        // Setup storage for logout sync (user forgot to check Remember Me)
        `when`(repository.getSavedUsername()).thenReturn(null)
        
        viewModel.onLogoutClicked()
        
        assertFalse(viewModel.uiState.value.status is LoginStatus.Success)
        assertEquals("", viewModel.uiState.value.username) // Should be cleared as per storage
    }

    @Test
    fun `Lockout after 3 failures`() = runTest {
        `when`(repository.login(anyString(), anyString())).thenReturn(AuthResult.InvalidCredentials("Incorrect password"))
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("123456")

        repeat(3) {
            viewModel.onLoginClicked()
        }

        assertTrue(viewModel.uiState.value.status is LoginStatus.LockedOut)
    }

    @Test
    fun `Single failure shows attempt count`() = runTest {
        `when`(repository.login(anyString(), anyString())).thenReturn(AuthResult.InvalidCredentials("Incorrect password"))
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("123456")

        viewModel.onLoginClicked()

        val status = viewModel.uiState.value.status
        assertTrue(status is LoginStatus.Error)
        assertEquals("Incorrect password. Attempt 1/3", (status as LoginStatus.Error).message)
    }

    @Test
    fun `Remember me saves both token and credentials`() = runTest {
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("123456")
        viewModel.onRememberMeChanged(true)
        `when`(repository.login("admin", "123456")).thenReturn(AuthResult.Success("token"))

        viewModel.onLoginClicked()

        verify(repository).saveToken("token")
        verify(repository).saveCredentials("admin", "123456")
    }
}
