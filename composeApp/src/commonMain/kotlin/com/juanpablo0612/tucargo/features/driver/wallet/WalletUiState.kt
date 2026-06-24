package com.juanpablo0612.tucargo.features.driver.wallet

import androidx.compose.runtime.Immutable

@Immutable
sealed interface WalletUiState {
    data object Loading : WalletUiState
    
    data class Success(
        val commissionOwed: Long,
        val ivaOwed: Long,
        val ssOwed: Long,
        val totalOwed: Long,
        val gateState: DispatchGateState,
        val maxDebtLimit: Long
    ) : WalletUiState
    
    data class Error(val message: String) : WalletUiState
}
