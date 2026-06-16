package com.juanpablo0612.tucargo.features.auth.register

import com.juanpablo0612.tucargo.domain.model.UserRole

sealed interface RegisterAction {
    data object Register : RegisterAction
    data class SelectRole(val role: UserRole) : RegisterAction
}
