package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.core.location.LocationProvider
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
                        // Evitamos actualizaciones si la distancia es mínima (optimización de batería/datos)
                        distanceBetween(old.latitude, old.longitude, new.latitude, new.longitude) < 0.0001
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
                // En producción, usar un Logger adecuado
                println("Error en tracking: ${e.message}")
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

    /**
     * Cálculo simple de distancia (Euclidiana) para evitar spam de actualizaciones idénticas.
     * Para precisión real, se usaría Haversine.
     */
    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dx = lat1 - lat2
        val dy = lon1 - lon2
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
