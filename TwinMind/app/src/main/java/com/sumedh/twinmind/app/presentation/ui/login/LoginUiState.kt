package com.sumedh.twinmind.app.presentation.ui.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val signInSuccess: Boolean = false,
    val error: String? = null
)