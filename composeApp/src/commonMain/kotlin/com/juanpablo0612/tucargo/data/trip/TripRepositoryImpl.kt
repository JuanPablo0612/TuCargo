package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class TripRepositoryImpl(
    firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers
) : TripRepository {

    private val tripsCollection = firestore.collection("trips")

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

    override suspend fun createTrip(trip: Trip): Result<String> = safeCall {
        withContext(dispatchers.io) {
            val doc = tripsCollection.document
            val id = doc.id
            doc.set(trip.copy(id = id).toDto())
            id
        }
    }

    override suspend fun updateTripStatus(tripId: String, status: TripStatus): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            tripsCollection.document(tripId).update(
                mapOf(
                    "status" to status.name,
                    "completed_at" to if (status == TripStatus.COMPLETED)
                        Clock.System.now().toEpochMilliseconds()
                    else null,
                )
            )
        }
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

    override fun observeDriverActiveTrips(driverId: String): Flow<List<Trip>> =
        tripsCollection
            .where { "driver_id" equalTo driverId }
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents
                    .map { it.data<TripDto>().toDomain() }
                    .filter { it.status in listOf(
                        TripStatus.ASSIGNED,
                        TripStatus.ON_WAY,
                        TripStatus.ARRIVED_PICKUP,
                        TripStatus.IN_PROGRESS
                    )}
            }
}
