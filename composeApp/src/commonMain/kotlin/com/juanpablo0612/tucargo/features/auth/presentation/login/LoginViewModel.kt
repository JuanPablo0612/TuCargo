package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    val emailState = TextFieldState()
    val passwordState = TextFieldState()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.ValidateEmail -> validateEmail()
            is LoginAction.ValidatePassword -> validatePassword()
            is LoginAction.Login -> onLogin()
        }
    }

    private fun validateEmail() {
    }

    private fun validatePassword() {
    }

    private fun onLogin() {
    }
}