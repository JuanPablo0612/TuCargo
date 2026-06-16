package com.juanpablo0612.tucargo.features.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.usecase.trip.GetClientTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.auth.LogoutUseCase
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientHomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getClientTripsUseCase: GetClientTripsUseCase,
    private val logoutUseCase: LogoutUseCase
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
            ClientHomeAction.SignOut -> viewModelScope.launch { logoutUseCase() }
            is ClientHomeAction.OnLocationUpdated -> _uiState.update {
                it.copy(userLatitude = action.latitude, userLongitude = action.longitude)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getCurrentUserUseCase().fold(
                onSuccess = { user -> _uiState.update { it.copy(user = user) } },
                onFailure = { _uiState.update { it.copy(error = ClientHomeError.LoadUserError) } }
            )
            loadRecentTrips()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadRecentTrips() {
        val userId = getCurrentUserIdUseCase() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrips = true) }
            getClientTripsUseCase(userId).fold(
                onSuccess = { trips ->
                    _uiState.update { it.copy(recentTrips = trips.toPersistentList(), isLoadingTrips = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(error = ClientHomeError.LoadTripsError, isLoadingTrips = false) }
                }
            )
        }
    }
}
