package com.juanpablo0612.tucargo.core.location

import kotlinx.serialization.Serializable

@Serializable
data class LocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float? = null,
    val timestamp: Long = 0L,
    val speedMs: Float? = null,
    val bearingDeg: Float? = null
)

interface LocationProvider {
    /**
     * Devuelve un Flow con actualizaciones de ubicación.
     * En una implementación real, esto vendría de FusedLocationProvider (Android) o CoreLocation (iOS).
     */
    fun getLocations(): kotlinx.coroutines.flow.Flow<LocationUpdate>
    
    suspend fun getCurrentLocation(): LocationUpdate?
}
