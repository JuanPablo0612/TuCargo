package com.juanpablo0612.tucargo.data.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlin.time.Instant

class TrackingRepositoryImpl(
    private val database: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) : TrackingRepository {

    override suspend fun writeLocation(driverId: String, location: DriverLocation): Result<Unit> =
        safeCall {
            coroutineScope {
                launch {
                    database.reference("driver_locations/$driverId").setValue(location.toRtdbMap())
                }
                launch {
                    firestore.collection("drivers").document(driverId).update(
                        mapOf(
                            "lastLocationLat" to location.lat,
                            "lastLocationLng" to location.lng,
                            "lastLocationAt" to FieldValue.serverTimestamp
                        )
                    )
                }
                if (location.tripId != null) {
                    launch {
                        firestore
                            .collection("trips")
                            .document(location.tripId)
                            .collection("trip_locations")
                            .add(
                                mapOf(
                                    "lat" to location.lat,
                                    "lng" to location.lng,
                                    "accuracyM" to location.accuracyM,
                                    "capturedAt" to location.capturedAt.toEpochMilliseconds(),
                                    "receivedAt" to FieldValue.serverTimestamp
                                )
                            )
                    }
                }
            }
        }

    override suspend fun writeLocationBatch(
        driverId: String,
        locations: List<DriverLocation>
    ): Result<Unit> = safeCall {
        if (locations.isEmpty()) return@safeCall
        val latest = locations.maxBy { it.capturedAt }
        coroutineScope {
            launch {
                database.reference("driver_locations/$driverId").setValue(latest.toRtdbMap())
            }
            val tripId = latest.tripId
            if (tripId != null) {
                val tripLocationsRef = firestore
                    .collection("trips")
                    .document(tripId)
                    .collection("trip_locations")
                locations.forEach { loc ->
                    launch {
                        tripLocationsRef.add(
                            mapOf(
                                "lat" to loc.lat,
                                "lng" to loc.lng,
                                "accuracyM" to loc.accuracyM,
                                "capturedAt" to loc.capturedAt.toEpochMilliseconds(),
                                "receivedAt" to FieldValue.serverTimestamp
                            )
                        )
                    }
                }
            }
        }
    }

    override fun observeDriverLocation(driverId: String): Flow<DriverLocation> =
        database.reference("driver_locations/$driverId")
            .valueEvents
            .mapNotNull { snapshot ->
                @Suppress("UNCHECKED_CAST")
                val value = snapshot.value as? Map<String, Any?> ?: return@mapNotNull null
                DriverLocation(
                    lat = (value["lat"] as? Double) ?: return@mapNotNull null,
                    lng = (value["lng"] as? Double) ?: return@mapNotNull null,
                    accuracyM = ((value["accuracyM"] as? Double)?.toFloat()) ?: 0f,
                    speedKph = (value["speedKph"] as? Double)?.toFloat(),
                    headingDeg = (value["headingDeg"] as? Double)?.toFloat(),
                    capturedAt = Instant.fromEpochMilliseconds(
                        (value["capturedAt"] as? Long)
                            ?: (value["capturedAt"] as? Double)?.toLong()
                            ?: return@mapNotNull null
                    ),
                    tripId = value["tripId"] as? String
                )
            }

    private fun DriverLocation.toRtdbMap(): Map<String, Any?> = mapOf(
        "lat" to lat,
        "lng" to lng,
        "accuracyM" to accuracyM,
        "speedKph" to speedKph,
        "headingDeg" to headingDeg,
        "capturedAt" to capturedAt.toEpochMilliseconds(),
        "tripId" to tripId
    )
}
