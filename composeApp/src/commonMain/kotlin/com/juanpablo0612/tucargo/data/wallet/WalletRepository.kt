package com.juanpablo0612.tucargo.data.wallet

import com.juanpablo0612.tucargo.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para la gestión del monedero pospago de conductores.
 * Define las operaciones necesarias para consultar balances, perfiles fiscales e historial.
 */
interface WalletRepository {

    /**
     * Obtiene el balance actual de la billetera del conductor.
     */
    suspend fun getWalletBalance(driverId: String): Result<WalletBalance>

    /**
     * Observa cambios en tiempo real del balance de la billetera.
     */
    fun observeWalletBalance(driverId: String): Flow<WalletBalance>

    /**
     * Obtiene el perfil fiscal configurado por el conductor.
     */
    suspend fun getTaxProfile(driverId: String): Result<DriverTaxProfile>

    /**
     * Actualiza el perfil fiscal del conductor.
     */
    suspend fun updateTaxProfile(driverId: String, profile: DriverTaxProfile): Result<Unit>

    /**
     * Obtiene el historial de transacciones (libro mayor) de comisiones.
     */
    suspend fun getLedgerHistory(driverId: String, limit: Int = 50): Result<List<CommissionTransaction>>

    /**
     * Obtiene el historial de liquidaciones realizadas.
     */
    suspend fun getSettlementHistory(driverId: String, limit: Int = 20): Result<List<Settlement>>

    /**
     * Obtiene la configuración global del sistema para el monedero.
     */
    suspend fun getWalletConfig(): Result<WalletConfig>
}
