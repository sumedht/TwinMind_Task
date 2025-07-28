package com.sumedh.twinmind.app.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.sumedh.twinmind.app.domain.usecase.GetCurrentUserUseCase
import com.sumedh.twinmind.app.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.sumedh.twinmind.app.util.Result
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        if (getCurrentUserUseCase() != null) {
            _uiState.value = LoginUiState(signInSuccess = true)
        }
    }

    fun onSignInResult(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            when (val result = signInUseCase(credential)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState(signInSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState(error = result.exception.message ?: "An unknown error occurred")
                }
            }
        }
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}