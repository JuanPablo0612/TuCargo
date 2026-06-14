package com.juanpablo0612.tucargo.testutil

import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.location.LocationUpdate
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.config.ConfigRepository
import com.juanpablo0612.tucargo.data.config.SystemConfig
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.data.quote.QuoteRepository
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import com.juanpablo0612.tucargo.domain.model.QuoteResult
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripOffer
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
    var clientTripsResult: Result<List<Trip>> = Result.success(emptyList())
    var driverTripsResult: Result<List<Trip>> = Result.success(emptyList())
    var createTripResult: Result<String> = Result.success("id")
    var requestTripResult: Result<Pair<String, String>> = Result.success("id" to "1234")
    var updateTripStatusResult: Result<Unit> = Result.success(Unit)
    var acceptTripResult: Result<Unit> = Result.success(Unit)
    var acceptOfferResult: Result<Unit> = Result.success(Unit)
    var rejectOfferResult: Result<Unit> = Result.success(Unit)
    var getTripResult: Result<Trip> = Result.failure(Exception())
    val tripFlow = MutableStateFlow(Trip())
    val activeTripsFlow = MutableStateFlow<List<Trip>>(emptyList())
    val availableTripsFlow = MutableStateFlow<List<Trip>>(emptyList())
    val activeOfferFlow = MutableStateFlow<TripOffer?>(null)

    var lastCreatedTrip: Trip? = null
    var lastStatusUpdate: Triple<String, TripStatus, TripStatus>? = null
    var lastAcceptedTripId: String? = null
    var lastRequestedTrip: Triple<String, String, Boolean>? = null

    override suspend fun getClientTrips(clientId: String, limit: Int): Result<List<Trip>> = clientTripsResult
    override suspend fun getDriverTrips(driverId: String, limit: Int): Result<List<Trip>> = driverTripsResult
    override suspend fun createTrip(trip: Trip): Result<String> {
        lastCreatedTrip = trip
        return createTripResult
    }
    override suspend fun requestTrip(
        quoteId: String,
        cargoDescription: String,
        weightConfirmed: Boolean
    ): Result<Pair<String, String>> {
        lastRequestedTrip = Triple(quoteId, cargoDescription, weightConfirmed)
        return requestTripResult
    }
    override suspend fun updateTripStatus(tripId: String, from: TripStatus, to: TripStatus): Result<Unit> {
        lastStatusUpdate = Triple(tripId, from, to)
        return updateTripStatusResult
    }
    override suspend fun acceptTrip(tripId: String, driverId: String, driverName: String, driverPlate: String): Result<Unit> {
        lastAcceptedTripId = tripId
        return acceptTripResult
    }
    override suspend fun acceptOffer(tripId: String, offerId: String): Result<Unit> = acceptOfferResult
    override suspend fun rejectOffer(tripId: String, offerId: String): Result<Unit> = rejectOfferResult
    override suspend fun getTrip(tripId: String): Result<Trip> = getTripResult
    override suspend fun updateDriverLocation(tripId: String, lat: Double, lng: Double): Result<Unit> = Result.success(Unit)
    override fun observeTrip(tripId: String): Flow<Trip> = tripFlow
    override fun observeDriverActiveTrips(driverId: String): Flow<List<Trip>> = activeTripsFlow
    override fun observeAvailableTrips(limit: Int): Flow<List<Trip>> = availableTripsFlow
    override fun observeActiveOffer(driverId: String): Flow<TripOffer?> = activeOfferFlow
}

class FakeUserRepository : UserRepository {
    var currentUser: Result<User> = Result.success(User(id = "user-1"))
    var currentUserId: String? = "user-1"
    var updateDriverStatusResult: Result<Unit> = Result.success(Unit)
    var pendingDriversResult: Result<List<User>> = Result.success(emptyList())
    var setDriverVerifiedResult: Result<Unit> = Result.success(Unit)
    var lastVerifiedDriver: Pair<String, Boolean>? = null
    val userFlow = MutableStateFlow<User?>(null)

    override suspend fun updateDriverStatus(userId: String, isOnline: Boolean): Result<Unit> = updateDriverStatusResult
    override fun getCurrentUserId(): String? = currentUserId
    override fun isUserLoggedIn(): Boolean = currentUserId != null
    override suspend fun getCurrentUser(): Result<User> = currentUser
    override suspend fun createUser(user: User): Result<Unit> = Result.success(Unit)
    override suspend fun updateUser(user: User): Result<Unit> = Result.success(Unit)
    override fun observeCurrentUser(): Flow<User?> = userFlow
    override suspend fun getPendingDrivers(): Result<List<User>> = pendingDriversResult
    override suspend fun setDriverVerified(userId: String, verified: Boolean): Result<Unit> {
        lastVerifiedDriver = userId to verified
        return setDriverVerifiedResult
    }
}

class FakeConfigRepository : ConfigRepository {
    var configResult: Result<SystemConfig> = Result.success(SystemConfig())
    override suspend fun getSystemConfig(): Result<SystemConfig> = configResult
}

class FakeDocumentRepository : DocumentRepository {
    var uploadResult: Result<Unit> = Result.success(Unit)
    var documentsResult: Result<List<KycDocument>> = Result.success(emptyList())
    var updateStatusResult: Result<Unit> = Result.success(Unit)
    var lastStatusUpdate: Triple<KycDocumentType, KycStatus, String?>? = null
    val documentsFlow = MutableStateFlow<List<KycDocument>>(emptyList())
    var flowError: Throwable? = null

    override suspend fun uploadDocument(userId: String, type: KycDocumentType, imageBytes: ByteArray): Result<Unit> = uploadResult
    override suspend fun getDocumentsForUser(userId: String): Result<List<KycDocument>> = documentsResult
    override fun observeDocumentsForUser(userId: String): Flow<List<KycDocument>> {
        val error = flowError
        return if (error != null) flow { throw error } else documentsFlow
    }
    override suspend fun updateDocumentStatus(
        userId: String,
        type: KycDocumentType,
        status: KycStatus,
        rejectionReason: String?
    ): Result<Unit> {
        lastStatusUpdate = Triple(type, status, rejectionReason)
        return updateStatusResult
    }
}

class FakeQuoteRepository : QuoteRepository {
    var createQuoteResult: Result<QuoteResult> = Result.failure(Exception("Not initialized"))
    var lastCreateQuoteCall: Map<String, Any>? = null

    override suspend fun createQuote(
        clientId: String,
        originLat: Double,
        originLng: Double,
        originAddr: String,
        destLat: Double,
        destLng: Double,
        destAddr: String
    ): Result<QuoteResult> {
        lastCreateQuoteCall = mapOf(
            "clientId" to clientId,
            "originLat" to originLat,
            "originLng" to originLng,
            "originAddr" to originAddr,
            "destLat" to destLat,
            "destLng" to destLng,
            "destAddr" to destAddr
        )
        return createQuoteResult
    }
}
