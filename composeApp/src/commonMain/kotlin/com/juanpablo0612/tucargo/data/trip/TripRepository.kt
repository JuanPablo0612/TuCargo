package com.juanpablo0612.tucargo.data.trip

interface TripRepository {
    suspend fun getClientTrips(clientId: String, limit: Int = 5): Result<List<Trip>>
    suspend fun createTrip(trip: Trip): Result<String>
    suspend fun updateTripStatus(tripId: String, status: TripStatus): Result<Unit>
    suspend fun getTrip(tripId: String): Result<Trip>
    suspend fun updateDriverLocation(tripId: String, lat: Double, lng: Double): Result<Unit>
}