package com.juanpablo0612.tucargo.data.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {
    suspend fun writeLocation(driverId: String, location: DriverLocation): Result<Unit>
    suspend fun writeLocationBatch(driverId: String, locations: List<DriverLocation>): Result<Unit>
    fun observeDriverLocation(driverId: String): Flow<DriverLocation>
}
