// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/features/auth/presentation/register/RegisterViewModel.kt

package com.juanpablo0612.tucargo.features.auth.presentation.register

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

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

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
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(email, password).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { e ->
                    val msg = when (e) {
                        is DataException.UserAlreadyExists -> "Este correo ya está registrado"
                        is DataException.Network -> "Error de red, intenta de nuevo"
                        else -> e.message ?: "Error desconocido"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
            )
        }
    }
}
