package com.juanpablo0612.tucargo.domain.model

enum class CommissionTransactionType {
    COMMISSION_ACCRUAL, // Comisión por viaje completado
    SETTLEMENT_PAYMENT,  // Pago realizado por el conductor
    ADJUSTMENT,          // Ajuste manual por soporte
}
