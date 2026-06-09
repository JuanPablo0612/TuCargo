package com.juanpablo0612.tucargo.features.driver.home.presentation

sealed interface DriverHomeAction {
    data class ToggleAvailability(val available: Boolean) : DriverHomeAction
}
