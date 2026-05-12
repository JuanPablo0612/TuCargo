package com.juanpablo0612.tucargo.features.driver.home.presentation

import com.juanpablo0612.tucargo.data.trip.Trip

data class DriverHomeState(
    val isLoading: Boolean = false,
    val driverName: String = "",
    val isAvailable: Boolean = false, // Botón de disponibilidad
    val balance: Double = 0.0,        // Balance actual
    val totalTrips: Int = 0,         // Viajes totales
    val activeTrips: List<Trip> = emptyList(), // Viajes activos[cite: 1]
    val error: String? = null
)