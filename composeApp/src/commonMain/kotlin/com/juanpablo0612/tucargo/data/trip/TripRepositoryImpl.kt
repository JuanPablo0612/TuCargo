package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripOffer
import com.juanpablo0612.tucargo.domain.model.TripStatus
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

private val DELIVERY_CODE_INVALID_REGEX = Regex("DELIVERY_CODE_INVALID:(\\d+)")

class TripRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
    private val functions: FirebaseFunctions
) : TripRepository {

    private val tripsCollection = firestore.collection("trips")
    private val tripOffersCollection = firestore.collection("trip_offers")

    override suspend fun getClientTrips(clientId: String, limit: Int): Result<List<Trip>> = safeCall {
        withContext(dispatchers.io) {
            tripsCollection
                .where { "client_id" equalTo clientId }
                .orderBy("created_at", Direction.DESCENDING)
                .limit(limit)
                .get()
                .documents
                .map { it.data<TripDto>().toDomain() }
        }
    }

    override suspend fun getDriverTrips(driverId: String, limit: Int): Result<List<Trip>> = safeCall {
        withContext(dispatchers.io) {
            tripsCollection
                .where { "driver_id" equalTo driverId }
                .orderBy("created_at", Direction.DESCENDING)
                .limit(limit)
                .get()
                .documents
                .map { it.data<TripDto>().toDomain() }
        }
    }

    override suspend fun createTrip(trip: Trip): Result<String> = safeCall {
        withContext(dispatchers.io) {
            val doc = tripsCollection.document
            val id = doc.id
            doc.set(
                trip.copy(
                    id = id,
                    createdAt = Clock.System.now().toEpochMilliseconds()
                ).toDto()
            )
            id
        }
    }

    override suspend fun requestTrip(
        quoteId: String,
        cargoDescription: String,
        weightConfirmed: Boolean
    ): Result<Pair<String, String>> = safeCall {
        val callable = functions.httpsCallable("requestTrip")
        val response = callable.invoke<Map<String, Any?>, Map<String, Any?>>(
            mapOf(
                "quoteId" to quoteId,
                "cargoDescription" to cargoDescription,
                "weightConfirmed" to weightConfirmed
            )
        )
        val data = response.data ?: throw AppError.DataCorruption("Empty response from requestTrip")
        val errorMsg = data["code"] as? String
        when (errorMsg) {
            "QUOTE_EXPIRED" -> throw AppError.Trip.QuoteExpired
            "QUOTE_ALREADY_USED" -> throw AppError.Trip.QuoteAlreadyUsed
        }
        val tripId = data["tripId"] as? String ?: throw AppError.DataCorruption("Missing tripId")
        val deliveryCode = data["deliveryCode"] as? String ?: throw AppError.DataCorruption("Missing deliveryCode")
        Pair(tripId, deliveryCode)
    }

    override suspend fun updateTripStatus(
        tripId: String,
        from: TripStatus,
        to: TripStatus
    ): Result<Unit> = safeCall {
        when (to) {
            TripStatus.AT_PICKUP, TripStatus.IN_TRANSIT, TripStatus.AT_DROPOFF -> {
                val action = when (to) {
                    TripStatus.AT_PICKUP -> "ARRIVE_PICKUP"
                    TripStatus.IN_TRANSIT -> "START_TRIP"
                    TripStatus.AT_DROPOFF -> "ARRIVE_DROPOFF"
                    else -> throw AppError.Trip.InvalidTransition
                }
                val callable = functions.httpsCallable("updateTripStatus")
                callable.invoke<Map<String, Any?>, Map<String, Any?>>(
                    mapOf("tripId" to tripId, "action" to action)
                )
            }
            TripStatus.CANCELLED_CLIENT -> withContext(dispatchers.io) {
                val docRef = tripsCollection.document(tripId)
                firestore.runTransaction {
                    val dto = get(docRef).data<TripDto>()
                    if (dto.status != from.name) throw AppError.Trip.InvalidTransition
                    update(
                        docRef,
                        "status" to TripStatus.CANCELLED_CLIENT.name,
                        "cancelled_by" to "CLIENT"
                    )
                }
            }
            else -> throw AppError.Trip.InvalidTransition
        }
        Unit
    }

    override suspend fun completeTrip(tripId: String, deliveryCode: String): Result<Unit> = safeCall {
        try {
            val callable = functions.httpsCallable("completeTrip")
            callable.invoke<Map<String, Any?>, Map<String, Any?>>(
                mapOf("tripId" to tripId, "deliveryCode" to deliveryCode)
            )
            Unit
        } catch (e: Exception) {
            val msg = e.message ?: throw e
            when {
                msg.contains("DELIVERY_CODE_LOCKED") -> throw AppError.Trip.DeliveryCodeLocked
                msg.contains("INVALID_CODE_FORMAT") -> throw AppError.Trip.InvalidCodeFormat
                else -> {
                    val match = DELIVERY_CODE_INVALID_REGEX.find(msg)
                    if (match != null) {
                        val remaining = match.groupValues[1].toIntOrNull() ?: 0
                        throw AppError.Trip.DeliveryCodeInvalid(remaining)
                    }
                    throw e
                }
            }
        }
    }

    override suspend fun acceptTrip(
        tripId: String,
        driverId: String,
        driverName: String,
        driverPlate: String
    ): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            val docRef = tripsCollection.document(tripId)
            firestore.runTransaction {
                val dto = get(docRef).data<TripDto>()
                if (dto.status != TripStatus.REQUESTED.name || dto.driverId != null) {
                    throw AppError.Trip.AlreadyTaken
                }
                update(
                    docRef,
                    "status" to TripStatus.ACCEPTED.name,
                    "driver_id" to driverId,
                    "driver_name" to driverName,
                    "driver_plate" to driverPlate
                )
            }
            Unit
        }
    }

    override suspend fun acceptOffer(tripId: String, offerId: String): Result<Unit> = safeCall {
        val callable = functions.httpsCallable("acceptOffer")
        callable.invoke<Map<String, Any?>, Map<String, Any?>>(
            mapOf("tripId" to tripId, "offerId" to offerId)
        )
        Unit
    }

    override suspend fun rejectOffer(tripId: String, offerId: String): Result<Unit> = safeCall {
        val callable = functions.httpsCallable("rejectOffer")
        callable.invoke<Map<String, Any?>, Map<String, Any?>>(
            mapOf("tripId" to tripId, "offerId" to offerId)
        )
        Unit
    }

    override suspend fun getTrip(tripId: String): Result<Trip> = safeCall {
        withContext(dispatchers.io) {
            tripsCollection.document(tripId).get().data<TripDto>().toDomain()
        }
    }

    override suspend fun updateDriverLocation(tripId: String, lat: Double, lng: Double): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            tripsCollection.document(tripId).update(
                mapOf(
                    "driver_last_lat" to lat,
                    "driver_last_lng" to lng,
                    "last_location_update" to Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }

    override fun observeTrip(tripId: String): Flow<Trip> =
        tripsCollection.document(tripId).snapshots
            .map { it.data<TripDto>().toDomain() }

    override fun observeDriverActiveTrips(driverId: String): Flow<List<Trip>> =
        tripsCollection
            .where { "driver_id" equalTo driverId }
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents
                    .map { it.data<TripDto>().toDomain() }
                    .filter { it.status in listOf(
                        TripStatus.ACCEPTED,
                        TripStatus.AT_PICKUP,
                        TripStatus.IN_TRANSIT,
                        TripStatus.AT_DROPOFF
                    )}
            }

    override fun observeAvailableTrips(limit: Int): Flow<List<Trip>> =
        tripsCollection
            .where { "status" equalTo TripStatus.REQUESTED.name }
            .orderBy("created_at", Direction.DESCENDING)
            .limit(limit)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.map { it.data<TripDto>().toDomain() }
            }

    override fun observeActiveOffer(driverId: String): Flow<TripOffer?> =
        tripOffersCollection
            .where { "driver_id" equalTo driverId }
            .where { "response" equalTo "PENDING" }
            .snapshots
            .map { querySnapshot ->
                val now = Clock.System.now().toEpochMilliseconds()
                querySnapshot.documents
                    .map { it.data<TripOfferDto>().toDomain() }
                    .firstOrNull { it.expiresAt > now }
            }
}
