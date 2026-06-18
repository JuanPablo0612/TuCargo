package com.juanpablo0612.tucargo.data.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {
    /** Live position for the client map — Realtime Database only. */
    suspend fun writeLocation(driverId: String, location: DriverLocation): Result<Unit>
    suspend fun writeLocationBatch(driverId: String, locations: List<DriverLocation>): Result<Unit>

    /**
     * Throttled write of the driver's coordinates to their `users` doc
     * (`last_lat`/`last_lng`/`last_location_at`) so dispatchTrip can run
     * nearest-driver matching. Far lower frequency than [writeLocation].
     */
    suspend fun updateDispatchLocation(driverId: String, lat: Double, lng: Double): Result<Unit>

    /** Removes the driver's live-location node when they go offline. */
    suspend fun clearLocation(driverId: String): Result<Unit>

    fun observeDriverLocation(driverId: String): Flow<DriverLocation>
}
