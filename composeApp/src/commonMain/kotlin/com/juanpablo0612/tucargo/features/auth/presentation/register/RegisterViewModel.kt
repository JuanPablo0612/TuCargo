package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.validation.EmailValidator
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.core.validation.PasswordValidator
import com.juanpablo0612.tucargo.core.validation.PhoneValidator
import com.juanpablo0612.tucargo.domain.model.AuthError
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
    var selectedRole by mutableStateOf("CLIENT")

    fun onNavigated() {
        _uiState.update { it.copy(successRole = null) }
    }

    fun onRegister() {
        val name = nameState.text.toString().trim()
        val email = emailState.text.toString().trim()
        val phone = phoneState.text.toString().trim()
        val password = passwordState.text.toString()

        val nameError: FieldError? = if (name.isBlank()) FieldError.NameRequired else null
        val emailError: FieldError? = when {
            email.isBlank() -> FieldError.EmailRequired
            !EmailValidator.isValid(email) -> FieldError.EmailInvalid
            else -> null
        }
        val phoneError: FieldError? = when {
            phone.isBlank() -> FieldError.PhoneRequired
            !PhoneValidator.isValid(phone) -> FieldError.PhoneInvalid
            else -> null
        }
        val passwordError: FieldError? = when {
            password.isBlank() -> FieldError.PasswordRequired
            password.length < 6 -> FieldError.PasswordTooShort
            !PasswordValidator.isValid(password) -> FieldError.PasswordWeak
            else -> null
        }

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
            registerUseCase(email, password, name, phone, selectedRole).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, successRole = user.role) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is AuthError.WeakPassword -> RegisterError.WeakPassword
                        is AuthError.EmailAlreadyInUse -> RegisterError.EmailAlreadyInUse
                        is AuthError.NetworkError -> RegisterError.NetworkError
                        else -> RegisterError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, authError = error) }
                }
            )
        }
    }
}
