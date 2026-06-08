package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.ui.event.UiEvent
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

    fun onRegister() {
        val name = nameState.text.toString().trim()
        val email = emailState.text.toString().trim()
        val phone = phoneState.text.toString().trim()
        val password = passwordState.text.toString()

        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(
                    error = RegisterError.FieldsRequired,
                    isNameError = name.isBlank(),
                    isEmailError = email.isBlank(),
                    isPhoneError = phone.isBlank(),
                    isPasswordError = password.isBlank(),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isNameError = false,
                    isEmailError = false,
                    isPhoneError = false,
                    isPasswordError = false,
                )
            }
            registerUseCase(email, password, name, phone, selectedRole).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(isLoading = false, navigationEvent = UiEvent(user.role))
                    }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is AuthError.WeakPassword -> RegisterError.WeakPassword
                        is AuthError.EmailAlreadyInUse -> RegisterError.UserAlreadyExists
                        is AuthError.NetworkError -> RegisterError.NetworkError
                        is AuthError.InvalidCredentials -> RegisterError.InvalidEmailFormat
                        is AuthError.Unknown -> when {
                            e.message?.contains("phone", ignoreCase = true) == true ->
                                RegisterError.InvalidPhoneFormat
                            e.message?.contains("name", ignoreCase = true) == true ->
                                RegisterError.FieldsRequired
                            else -> RegisterError.UnknownError
                        }
                        else -> RegisterError.UnknownError
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error,
                            isNameError = error is RegisterError.FieldsRequired,
                            isEmailError = error is RegisterError.InvalidEmailFormat,
                            isPhoneError = error is RegisterError.InvalidPhoneFormat,
                            isPasswordError = error is RegisterError.WeakPassword || error is RegisterError.PasswordTooShort,
                        )
                    }
                }
            )
        }
    }
}
