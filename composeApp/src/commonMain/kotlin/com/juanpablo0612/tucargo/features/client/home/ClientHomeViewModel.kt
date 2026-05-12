// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/features/client/home/ClientHomeViewModel.kt

package com.juanpablo0612.tucargo.features.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientHomeViewModel(
    private val userRepository: UserRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientHomeState())
    val uiState: StateFlow<ClientHomeState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onAction(action: ClientHomeAction) {
        when (action) {
            ClientHomeAction.LoadData -> loadData()
            ClientHomeAction.RefreshTrips -> loadRecentTrips()
            ClientHomeAction.NewTrip -> { }
            ClientHomeAction.SignOut -> viewModelScope.launch { userRepository.signOut() }
            is ClientHomeAction.OnLocationUpdated -> _uiState.update {
                it.copy(userLatitude = action.latitude, userLongitude = action.longitude)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            userRepository.getCurrentUser().fold(
                onSuccess = { user -> _uiState.update { it.copy(user = user) } },
                onFailure = { e -> _uiState.update { it.copy(errorMessage = e.message) } }
            )
            loadRecentTrips()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadRecentTrips() {
        val userId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrips = true) }
            tripRepository.getClientTrips(userId).fold(
                onSuccess = { trips ->
                    _uiState.update { it.copy(recentTrips = trips, isLoadingTrips = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message, isLoadingTrips = false) }
                }
            )
        }
    }
}
