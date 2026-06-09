package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.validation.EmailValidator
import com.juanpablo0612.tucargo.core.validation.FieldError
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

    fun onNavigated() {
        _uiState.update { it.copy(successRole = null) }
    }

    private fun validateEmail(): Boolean {
        val email = emailState.text.toString().trim()
        val error = when {
            email.isBlank() -> FieldError.EmailRequired
            !EmailValidator.isValid(email) -> FieldError.EmailInvalid
            else -> null
        }
        _uiState.update { it.copy(emailError = error) }
        return error == null
    }

    private fun validatePassword(): Boolean {
        val password = passwordState.text.toString()
        val error = when {
            password.isBlank() -> FieldError.PasswordRequired
            password.length < 6 -> FieldError.PasswordTooShort
            else -> null
        }
        _uiState.update { it.copy(passwordError = error) }
        return error == null
    }

    private fun onLogin() {
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        if (!emailValid || !passwordValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            loginUseCase(
                email = emailState.text.toString(),
                password = passwordState.text.toString()
            ).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, successRole = user.role) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is AuthError.InvalidCredentials -> LoginError.InvalidCredentials
                        is AuthError.NetworkError -> LoginError.NetworkError
                        else -> LoginError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, authError = error) }
                }
            )
        }
    }
}
