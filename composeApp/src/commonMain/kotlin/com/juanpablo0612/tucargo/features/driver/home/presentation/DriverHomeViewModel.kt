package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.data.trip.TripTrackingManager
import com.juanpablo0612.tucargo.data.trip.TripStatus
import com.juanpablo0612.tucargo.data.user.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriverHomeViewModel(
    private val userRepository: UserRepository,
    private val trackingManager: TripTrackingManager
) : ViewModel() {

    private val _state = MutableStateFlow(DriverHomeState())
    val state = _state.asStateFlow()

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    init {
        loadDriverData()
        observeActiveTrips()
    }

    private fun observeActiveTrips() {
        state.onEach { currentState ->
            val activeTrip = currentState.activeTrips.firstOrNull { 
                it.status == TripStatus.IN_PROGRESS || it.status == TripStatus.ON_WAY 
            }
            
            if (activeTrip != null) {
                trackingManager.startTracking(activeTrip.id)
            } else {
                trackingManager.stopTracking()
            }
        }.launchIn(viewModelScope)
    }

    private fun loadDriverData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            userRepository.getCurrentUser().onSuccess { user ->
                _state.update { it.copy(
                    isLoading = false,
                    // CAMBIO: Usamos los nombres de variables que definimos en la data class User
                    driverName = user.fullName,       // Antes: user.full_name
                    balance = user.walletBalance,     // Antes: user.wallet_balance
                    isAvailable = user.isOnline       // Antes: user.is_online[cite: 1]
                )}
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun toggleAvailability(available: Boolean) {
        viewModelScope.launch {
            // OPCIONAL: Bloquear si no está verificado (is_verified en tu DB)
            // if (!userIsVerified) { ... mostrar error ... return@launch }

            // 1. Actualización optimista
            _state.update { it.copy(isAvailable = available) }

            // 2. Persistencia usando el nombre de función correcto
            if (currentUserId.isNotEmpty()) {
                val result = userRepository.updateDriverStatus(currentUserId, available)

                result.onFailure {
                    _state.update { it.copy(
                        isAvailable = !available,
                        error = "Error de conexión con la base de datos"
                    )}
                }
            }
        }
    }
}