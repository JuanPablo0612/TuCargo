package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.ui.event.UiEvent
import com.juanpablo0612.tucargo.domain.model.AuthError
import com.juanpablo0612.tucargo.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    val emailState = TextFieldState()
    val passwordState = TextFieldState()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.ValidateEmail -> validateEmail()
            LoginAction.ValidatePassword -> validatePassword()
            LoginAction.Login -> onLogin()
        }
    }

    private fun validateEmail(): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,6}$")
        val isValid = emailRegex.matches(emailState.text.toString().trim())
        _uiState.update { it.copy(isEmailValid = isValid) }
        return isValid
    }

    private fun validatePassword(): Boolean {
        val isValid = passwordState.text.length >= 6
        _uiState.update { it.copy(isPasswordValid = isValid) }
        return isValid
    }

    private fun onLogin() {
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        if (!emailValid || !passwordValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            loginUseCase(
                email = emailState.text.toString(),
                password = passwordState.text.toString()
            ).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, navigationEvent = UiEvent(user.role)) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is AuthError.InvalidCredentials -> LoginError.InvalidCredentials
                        is AuthError.NetworkError -> LoginError.NetworkError
                        else -> LoginError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, loginError = error) }
                }
            )
        }
    }
}
