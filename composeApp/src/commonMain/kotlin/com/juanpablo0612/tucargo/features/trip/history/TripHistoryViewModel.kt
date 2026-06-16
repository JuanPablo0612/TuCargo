package com.juanpablo0612.tucargo.features.trip.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.usecase.GetClientTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetDriverTripsUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripHistoryViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getClientTripsUseCase: GetClientTripsUseCase,
    private val getDriverTripsUseCase: GetDriverTripsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripHistoryState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTrips()
    }

    fun onAction(action: TripHistoryAction) {
        when (action) {
            TripHistoryAction.Refresh -> loadTrips()
        }
    }

    private fun loadTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = getCurrentUserUseCase().getOrNull()
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = TripHistoryError.LoadError) }
                return@launch
            }
            val result = when (user.role) {
                UserRole.DRIVER -> getDriverTripsUseCase(user.id, limit = HISTORY_LIMIT)
                else -> getClientTripsUseCase(user.id, limit = HISTORY_LIMIT)
            }
            result.fold(
                onSuccess = { trips ->
                    _uiState.update { it.copy(isLoading = false, trips = trips.toImmutableList()) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = TripHistoryError.LoadError) }
                }
            )
        }
    }

    private companion object {
        const val HISTORY_LIMIT = 50
    }
}
