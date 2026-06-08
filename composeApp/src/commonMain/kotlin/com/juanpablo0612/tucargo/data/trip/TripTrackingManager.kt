package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.logging.logError
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Encargado de gestionar el tracking del conductor durante un viaje.
 * Sigue principios SOLID (Single Responsibility) al separar el tracking del repositorio y el ViewModel.
 */
class TripTrackingManager(
    private val tripRepository: TripRepository,
    private val locationProvider: LocationProvider,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private var trackingJob: Job? = null
    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    /**
     * Inicia el envío periódico de coordenadas.
     * @param tripId El ID del viaje en curso.
     * @param intervalMillis Intervalo entre actualizaciones (ej. cada 10 segundos).
     */
    fun startTracking(tripId: String, intervalMillis: Long = 10_000L) {
        if (_isTracking.value) return

        trackingJob = scope.launch {
            _isTracking.value = true
            try {
                // Obtenemos el flujo de ubicaciones del proveedor
                locationProvider.getLocations()
                    .distinctUntilChanged { old, new ->
                        haversineDistance(old.latitude, old.longitude, new.latitude, new.longitude) < 10.0
                    }
                    .collectLatest { location ->
                        // Enviamos a Firestore
                        tripRepository.updateDriverLocation(
                            tripId = tripId,
                            lat = location.latitude,
                            lng = location.longitude
                        )
                        delay(intervalMillis)
                    }
            } catch (e: Exception) {
                logError("TripTracking", "Error en tracking: ${e.message}")
            } finally {
                _isTracking.value = false
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _isTracking.value = false
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6_371_000.0
        val toRad = PI / 180.0
        val phi1 = lat1 * toRad
        val phi2 = lat2 * toRad
        val dPhi = (lat2 - lat1) * toRad
        val dLambda = (lon2 - lon1) * toRad
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
