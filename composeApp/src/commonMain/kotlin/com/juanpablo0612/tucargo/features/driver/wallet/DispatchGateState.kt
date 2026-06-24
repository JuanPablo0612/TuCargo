package com.juanpablo0612.tucargo.features.driver.wallet

enum class DispatchGateState {
    NORMAL,              // Deuda < 80%
    WARNING_80,          // Deuda entre 80% y 89% (Alerta preventiva)
    WARNING_90,          // Deuda entre 90% y 99% (Alerta crítica)
    BLOCKED_DUE_PROCESS  // Deuda >= 100% (Bloqueo de despacho)
}
