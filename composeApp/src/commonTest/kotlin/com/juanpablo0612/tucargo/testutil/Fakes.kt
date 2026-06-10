package com.juanpablo0612.tucargo.testutil

import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.location.LocationUpdate
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository : AuthRepository {
    var loginResult: Result<User> = Result.failure(Exception("Not initialized"))
    override suspend fun login(email: String, password: String): Result<User> = loginResult
    override suspend fun register(email: String, password: String, fullName: String, phone: String, role: UserRole): Result<User> = loginResult
    override suspend fun logout(): Result<Unit> = Result.success(Unit)
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun getCurrentUser(): User? = null
    override fun observeAuthState(): Flow<User?> = flowOf(null)
}

class FakeLocationProvider : LocationProvider {
    val locations = MutableStateFlow(LocationUpdate(0.0, 0.0))
    override fun getLocations(): Flow<LocationUpdate> = locations
    override suspend fun getCurrentLocation(): LocationUpdate? = locations.value
}

class FakeTripRepository : TripRepository {
    override suspend fun getClientTrips(clientId: String, limit: Int): Result<List<Trip>> = Result.success(emptyList())
    override suspend fun createTrip(trip: Trip): Result<String> = Result.success("id")
    override suspend fun updateTripStatus(tripId: String, status: TripStatus): Result<Unit> = Result.success(Unit)
    override suspend fun getTrip(tripId: String): Result<Trip> = Result.failure(Exception())
    override suspend fun updateDriverLocation(tripId: String, lat: Double, lng: Double): Result<Unit> = Result.success(Unit)
    override fun observeDriverActiveTrips(driverId: String): Flow<List<Trip>> = flowOf(emptyList())
}
