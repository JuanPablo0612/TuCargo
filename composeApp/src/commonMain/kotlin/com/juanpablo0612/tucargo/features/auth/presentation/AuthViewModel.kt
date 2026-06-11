package com.juanpablo0612.tucargo.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.model.DriverOnboardingStatus
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.usecase.GetDriverOnboardingStatusUseCase
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
    private val logoutUseCase: LogoutUseCase,
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase
) : ViewModel() {

    sealed interface AuthState {
        data object Loading : AuthState
        data class Authenticated(
            val user: User,
            val driverOnboardingStatus: DriverOnboardingStatus? = null
        ) : AuthState
        data object Unauthenticated : AuthState
    }

    val authState: StateFlow<AuthState> = observeAuthStateUseCase()
        .map { user ->
            if (user == null) return@map AuthState.Unauthenticated
            if (user.role == UserRole.DRIVER && !user.isVerified) {
                val status = getDriverOnboardingStatusUseCase()
                    .getOrElse { DriverOnboardingStatus.IncompleteVehicle }
                AuthState.Authenticated(user, driverOnboardingStatus = status)
            } else {
                AuthState.Authenticated(user)
            }
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
