package com.sumedh.twinmind.app.presentation.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AuthCredential
import com.sumedh.twinmind.app.data.model.User
import com.sumedh.twinmind.app.domain.usecase.GetCurrentUserUseCase
import com.sumedh.twinmind.app.domain.usecase.SignInUseCase
import com.sumedh.twinmind.app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel
    private val mockSignInUseCase: SignInUseCase = mock()
    private val mockGetCurrentUserUseCase: GetCurrentUserUseCase = mock()
    private val mockCredential: AuthCredential = mock()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSignInResult with success, uiState should be signInSuccess true`() = runTest {
        // Arrange
        val user = User("123", "Test User", "test@example.com")
        whenever(mockSignInUseCase.invoke(mockCredential)).thenReturn(Result.Success(user))
        viewModel = LoginViewModel(mockSignInUseCase, mockGetCurrentUserUseCase)

        // Act
        viewModel.onSignInResult(mockCredential)

        // Assert
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.signInSuccess).isTrue()
        assertThat(uiState.error).isNull()
    }

    @Test
    fun `onSignInResult with error, uiState should contain error message`() = runTest {
        // Arrange
        val errorMessage = "Authentication failed"
        whenever(mockSignInUseCase.invoke(mockCredential)).thenReturn(Result.Error(Exception(errorMessage)))
        viewModel = LoginViewModel(mockSignInUseCase, mockGetCurrentUserUseCase)

        // Act
        viewModel.onSignInResult(mockCredential)

        // Assert
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.signInSuccess).isFalse()
        assertThat(uiState.error).isEqualTo(errorMessage)
    }

    @Test
    fun `init with logged in user, uiState should be signInSuccess true`() {
        // Arrange
        val user = User("123", "Test User", "test@example.com")
        whenever(mockGetCurrentUserUseCase.invoke()).thenReturn(user)

        // Act
        viewModel = LoginViewModel(mockSignInUseCase, mockGetCurrentUserUseCase)

        // Assert
        val uiState = viewModel.uiState.value
        assertThat(uiState.signInSuccess).isTrue()
    }

    @Test
    fun `init with no user, uiState should be default`() {
        // Arrange
        whenever(mockGetCurrentUserUseCase.invoke()).thenReturn(null)

        // Act
        viewModel = LoginViewModel(mockSignInUseCase, mockGetCurrentUserUseCase)

        // Assert
        val uiState = viewModel.uiState.value
        assertThat(uiState.signInSuccess).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNull()
    }
}