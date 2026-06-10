package com.juanpablo0612.tucargo.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.usecase.LogoutUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    sealed interface AuthState {
        data object Loading : AuthState
        data class Authenticated(val user: User) : AuthState
        data object Unauthenticated : AuthState
    }

    val authState: StateFlow<AuthState> = observeAuthStateUseCase()
        .map { user ->
            if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading
        )

    private val _logoutError = MutableStateFlow<String?>(null)
    val logoutError = _logoutError.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            logoutUseCase().onFailure { e ->
                _logoutError.value = e.message
            }
        }
    }

    fun clearLogoutError() {
        _logoutError.value = null
    }
}
