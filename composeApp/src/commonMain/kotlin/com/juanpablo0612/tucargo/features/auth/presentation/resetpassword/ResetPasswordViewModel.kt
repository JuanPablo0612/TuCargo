package com.juanpablo0612.tucargo.features.auth.presentation.resetpassword

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.model.AuthError
import com.juanpablo0612.tucargo.domain.usecase.SendPasswordResetEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordState())
    val uiState: StateFlow<ResetPasswordState> = _uiState.asStateFlow()

    val emailState = TextFieldState()

    fun onSubmit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            sendPasswordResetEmailUseCase(emailState.text.toString()).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { e ->
                    val error = when (e) {
                        is AuthError.NetworkError -> ResetPasswordError.NetworkError
                        is AuthError.Unknown -> if (e.message?.contains("Email required") == true)
                            ResetPasswordError.EmailRequired
                        else
                            ResetPasswordError.UnknownError
                        else -> ResetPasswordError.UnknownError
                    }
                    _uiState.update { it.copy(isLoading = false, error = error) }
                }
            )
        }
    }
}
