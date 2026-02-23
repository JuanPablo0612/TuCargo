package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
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
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        val isValid = emailRegex.matches(emailState.text)
        _uiState.update {
            it.copy(isEmailValid = isValid)
        }
    }

    private fun validatePassword() {
        val isValid = passwordState.text.length >= 8
        _uiState.update {
            it.copy(isPasswordValid = isValid)
        }
    }

    private fun onLogin() {
    }
}