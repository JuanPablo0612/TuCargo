package com.juanpablo0612.tucargo.core.location

import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockLocationProvider : LocationProvider {
    override fun getLocations(): Flow<LocationUpdate> = flow {
        var lat = 4.6097 // Bogotá
        var lng = -74.0817
        while (true) {
            emit(LocationUpdate(lat, lng, timestamp = Clock.System.now().toEpochMilliseconds()))
            delay(5000)
            lat += 0.0001
            lng += 0.0001
        }
    }

    override suspend fun getCurrentLocation(): LocationUpdate {
        return LocationUpdate(4.6097, -74.0817, timestamp = Clock.System.now().toEpochMilliseconds())
    }
}
