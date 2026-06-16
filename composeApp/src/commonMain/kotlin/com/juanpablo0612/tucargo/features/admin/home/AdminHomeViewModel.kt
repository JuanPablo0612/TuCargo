package com.juanpablo0612.tucargo.features.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.usecase.admin.GetPendingDriversUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminHomeViewModel(
    private val getPendingDriversUseCase: GetPendingDriversUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPendingDrivers()
    }

    fun onAction(action: AdminHomeAction) {
        when (action) {
            AdminHomeAction.Refresh -> loadPendingDrivers()
        }
    }

    private fun loadPendingDrivers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getPendingDriversUseCase().fold(
                onSuccess = { drivers ->
                    _uiState.update {
                        it.copy(isLoading = false, pendingDrivers = drivers.toImmutableList())
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = AdminHomeError.LoadError) }
                }
            )
        }
    }
}
