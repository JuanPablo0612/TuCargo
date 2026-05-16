package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlin.time.Clock

class TripRepositoryImpl(firestore: FirebaseFirestore) : TripRepository {

    private val tripsCollection = firestore.collection("trips")

    override suspend fun getClientTrips(clientId: String, limit: Int): Result<List<Trip>> = safeCall {
        tripsCollection
            .where { "client_id" equalTo clientId }
            .orderBy("created_at", Direction.DESCENDING)
            .limit(limit)
            .get()
            .documents
            .map { it.data<Trip>() }
    }

    override suspend fun createTrip(trip: Trip): Result<String> = safeCall {
        val docRef = tripsCollection.add(trip)
        val id = docRef.id
        tripsCollection.document(id).set(trip.copy(id = id), merge = true)
        id
    }

    override suspend fun updateTripStatus(tripId: String, status: TripStatus): Result<Unit> = safeCall {
        tripsCollection.document(tripId).update(
            mapOf(
                "status" to status.name,
                "completed_at" to if (status == TripStatus.COMPLETED)
                    Clock.System.now().toEpochMilliseconds()
                else null,
            )
        )
    }

    override suspend fun getTrip(tripId: String): Result<Trip> = safeCall {
        tripsCollection.document(tripId).get().data<Trip>()
    }

    override suspend fun updateDriverLocation(tripId: String, lat: Double, lng: Double): Result<Unit> = safeCall {
        tripsCollection.document(tripId).update(
            mapOf(
                "driver_last_lat" to lat,
                "driver_last_lng" to lng,
                "last_location_update" to Clock.System.now().toEpochMilliseconds()
            )
        )
    }
}
