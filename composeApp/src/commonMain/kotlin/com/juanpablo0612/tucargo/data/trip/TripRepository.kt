package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    suspend fun getClientTrips(clientId: String, limit: Int = 5): Result<List<Trip>>
    suspend fun getDriverTrips(driverId: String, limit: Int = 5): Result<List<Trip>>
    suspend fun createTrip(trip: Trip): Result<String>

    /**
     * Transitions the trip from [from] to [to], failing with
     * [com.juanpablo0612.tucargo.domain.model.AppError.Trip.InvalidTransition]
     * if the stored status no longer matches [from].
     */
    suspend fun updateTripStatus(tripId: String, from: TripStatus, to: TripStatus): Result<Unit>

    /**
     * Atomically assigns the trip to the driver. Fails with
     * [com.juanpablo0612.tucargo.domain.model.AppError.Trip.AlreadyTaken]
     * when another driver got there first.
     */
    suspend fun acceptTrip(
        tripId: String,
        driverId: String,
        driverName: String,
        driverPlate: String
    ): Result<Unit>

    suspend fun getTrip(tripId: String): Result<Trip>
    suspend fun updateDriverLocation(tripId: String, lat: Double, lng: Double): Result<Unit>
    fun observeTrip(tripId: String): Flow<Trip>
    fun observeDriverActiveTrips(driverId: String): Flow<List<Trip>>
    fun observeAvailableTrips(limit: Int = 20): Flow<List<Trip>>
}
