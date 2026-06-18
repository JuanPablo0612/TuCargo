package com.juanpablo0612.tucargo.data.tracking

import com.juanpablo0612.tucargo.core.location.DriverLocation
import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Clock
import kotlin.time.Instant

class TrackingRepositoryImpl(
    private val database: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) : TrackingRepository {

    // Only the RTDB node feeds the client's live map; the old per-tick Firestore
    // writes (drivers/{id} mirror + trips/{id}/trip_locations breadcrumbs) were
    // never read and have been dropped. Dispatch matching is fed separately by
    // the throttled updateDispatchLocation.
    override suspend fun writeLocation(driverId: String, location: DriverLocation): Result<Unit> =
        safeCall {
            database.reference("driver_locations/$driverId").setValue(location.toRtdbMap())
        }

    override suspend fun writeLocationBatch(
        driverId: String,
        locations: List<DriverLocation>
    ): Result<Unit> = safeCall {
        if (locations.isEmpty()) return@safeCall
        val latest = locations.maxBy { it.capturedAt }
        database.reference("driver_locations/$driverId").setValue(latest.toRtdbMap())
    }

    override suspend fun updateDispatchLocation(
        driverId: String,
        lat: Double,
        lng: Double
    ): Result<Unit> = safeCall {
        firestore.collection("users").document(driverId).update(
            mapOf(
                "last_lat" to lat,
                "last_lng" to lng,
                "last_location_at" to Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun clearLocation(driverId: String): Result<Unit> = safeCall {
        database.reference("driver_locations/$driverId").removeValue()
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
