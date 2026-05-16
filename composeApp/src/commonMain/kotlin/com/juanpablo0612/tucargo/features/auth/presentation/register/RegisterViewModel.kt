package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.ui.event.UiEvent
import com.juanpablo0612.tucargo.data.common.DataException
import com.juanpablo0612.tucargo.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val registerUseCase: RegisterUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    val nameState = TextFieldState()
    val emailState = TextFieldState()
    val passwordState = TextFieldState()
    val confirmPasswordState = TextFieldState()

    fun onRegister() {
        val name = nameState.text.toString().trim()
        val email = emailState.text.toString().trim()
        val password = passwordState.text.toString()
        val confirmPassword = confirmPasswordState.text.toString()

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = RegisterError.FieldsRequired) }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = RegisterError.PasswordMismatch) }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(error = RegisterError.PasswordTooShort) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            registerUseCase(email, password, name).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, navigationEvent = UiEvent(Unit)) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is DataException.UserAlreadyExists -> RegisterError.UserAlreadyExists
                        is DataException.Network -> RegisterError.NetworkError
                        else -> RegisterError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, error = error) }
                }
            )
        }
    }
}
