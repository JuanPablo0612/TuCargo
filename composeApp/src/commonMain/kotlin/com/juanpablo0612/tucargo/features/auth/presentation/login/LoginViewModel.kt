// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/features/auth/presentation/login/LoginViewModel.kt

package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.common.DataException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

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
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        val isValid = emailRegex.matches(emailState.text)
        _uiState.update { it.copy(isEmailValid = isValid) }
        return isValid
    }

    private fun validatePassword(): Boolean {
        val isValid = passwordState.text.length >= 8
        _uiState.update { it.copy(isPasswordValid = isValid) }
        return isValid
    }

    private fun onLogin() {
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        if (!emailValid || !passwordValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            authRepository.login(
                email = emailState.text.toString(),
                password = passwordState.text.toString()
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is DataException.InvalidCredentials -> LoginError.InvalidCredentials
                        is DataException.Network -> LoginError.NetworkError
                        else -> LoginError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, loginError = error) }
                }
            )
        }
    }
}
