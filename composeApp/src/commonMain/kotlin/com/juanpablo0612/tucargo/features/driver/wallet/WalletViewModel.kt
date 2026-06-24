package com.juanpablo0612.tucargo.features.driver.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.data.wallet.WalletRepository
import com.juanpablo0612.tucargo.domain.usecase.user.GetCurrentUserIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la gestión del monedero del conductor.
 * Implementa la lógica de estados para el control de despacho basado en la deuda.
 */
class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletUiState>(WalletUiState.Loading)
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        subscribeToWalletUpdates()
    }

    /**
     * Se suscribe al flujo de datos de la billetera en tiempo real.
     * Carga la configuración inicial y luego observa los cambios de saldo.
     */
    private fun subscribeToWalletUpdates() {
        val driverId = getCurrentUserIdUseCase()
        if (driverId == null) {
            _uiState.value = WalletUiState.Error("Error: Sesión de usuario no encontrada.")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Obtener configuración global (límites de deuda)
                val config = walletRepository.getWalletConfig().getOrElse {
                    _uiState.value = WalletUiState.Error("No se pudo cargar la configuración del monedero.")
                    return@launch
                }

                val maxDebtLimit = config.maxCommissionDebt.amount.toLong()

                // 2. Observar el balance en tiempo real desde Firestore
                walletRepository.observeWalletBalance(driverId)
                    .catch { e ->
                        _uiState.value = WalletUiState.Error(e.message ?: "Error al sincronizar el monedero.")
                    }
                    .collect { balance ->
                        val totalOwed = balance.totalOwed.amount.toLong()
                        
                        _uiState.value = WalletUiState.Success(
                            commissionOwed = balance.commissionOwed.amount.toLong(),
                            ivaOwed = balance.ivaOwed.amount.toLong(),
                            ssOwed = balance.ssOwed.amount.toLong(),
                            totalOwed = totalOwed,
                            gateState = calculateGateState(totalOwed, maxDebtLimit),
                            maxDebtLimit = maxDebtLimit
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error("Ocurrió un error inesperado: ${e.message}")
            }
        }
    }

    /**
     * Lógica comercial para determinar si el despacho debe ser bloqueado o alertado.
     * Basado en el porcentaje de deuda acumulada frente al límite permitido.
     */
    private fun calculateGateState(totalOwed: Long, maxDebtLimit: Long): DispatchGateState {
        if (maxDebtLimit <= 0) return DispatchGateState.NORMAL
        
        val ratio = totalOwed.toDouble() / maxDebtLimit.toDouble()
        
        return when {
            ratio < 0.8 -> DispatchGateState.NORMAL
            ratio < 0.9 -> DispatchGateState.WARNING_80
            ratio < 1.0 -> DispatchGateState.WARNING_90
            else -> DispatchGateState.BLOCKED_DUE_PROCESS
        }
    }
}
