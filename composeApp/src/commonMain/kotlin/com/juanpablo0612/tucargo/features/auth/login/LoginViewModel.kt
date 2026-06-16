package com.juanpablo0612.tucargo.features.auth.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.core.validation.FormValidators
import com.juanpablo0612.tucargo.domain.model.AppError
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
        val error = FormValidators.email(emailState.text.toString())
        _uiState.update { it.copy(emailError = error) }
        return error == null
    }

    private fun validatePassword(): Boolean {
        val error = FormValidators.password(passwordState.text.toString())
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
                        is AppError.Auth.InvalidCredentials -> LoginError.InvalidCredentials
                        is AppError.Network -> LoginError.NetworkError
                        else -> LoginError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, authError = error) }
                }
            )
        }
    }
}
