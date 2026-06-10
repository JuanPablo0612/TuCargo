package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.core.validation.FormValidators
import com.juanpablo0612.tucargo.domain.model.AppError
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
    val phoneState = TextFieldState()
    val passwordState = TextFieldState()

    fun onAction(action: RegisterAction) {
        when (action) {
            RegisterAction.Register -> onRegister()
            is RegisterAction.SelectRole -> {
                _uiState.update { it.copy(selectedRole = action.role) }
            }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(successRole = null) }
    }

    private fun onRegister() {
        val name = nameState.text.toString().trim()
        val email = emailState.text.toString().trim()
        val phone = phoneState.text.toString().trim()
        val password = passwordState.text.toString()

        val nameError = FormValidators.required(name, FieldError.NameRequired)
        val emailError = FormValidators.email(email)
        val phoneError = FormValidators.phone(phone)
        val passwordError = FormValidators.password(password)

        _uiState.update {
            it.copy(
                nameError = nameError,
                emailError = emailError,
                phoneError = phoneError,
                passwordError = passwordError,
                authError = null
            )
        }

        if (listOf(nameError, emailError, phoneError, passwordError).any { it != null }) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            registerUseCase(email, password, name, phone, _uiState.value.selectedRole).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, successRole = user.role) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is AppError.Auth.WeakPassword -> RegisterError.WeakPassword
                        is AppError.Auth.EmailAlreadyInUse -> RegisterError.EmailAlreadyInUse
                        is AppError.Network -> RegisterError.NetworkError
                        else -> RegisterError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, authError = error) }
                }
            )
        }
    }
}
