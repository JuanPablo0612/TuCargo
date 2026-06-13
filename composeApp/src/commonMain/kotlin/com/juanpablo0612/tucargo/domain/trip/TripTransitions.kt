package com.juanpablo0612.tucargo.domain.trip

import com.juanpablo0612.tucargo.domain.model.TripStatus

/**
 * Single source of truth for the trip lifecycle, mirrored exactly by the
 * `update` rules on /trips in firestore.rules. Keep both in sync.
 */
val tripTransitions: Map<TripStatus, Set<TripStatus>> = mapOf(
    TripStatus.SEARCHING to setOf(TripStatus.ASSIGNED, TripStatus.CANCELLED),
    TripStatus.ASSIGNED to setOf(TripStatus.ON_WAY, TripStatus.CANCELLED),
    TripStatus.ON_WAY to setOf(TripStatus.ARRIVED_PICKUP),
    TripStatus.ARRIVED_PICKUP to setOf(TripStatus.IN_PROGRESS),
    TripStatus.IN_PROGRESS to setOf(TripStatus.COMPLETED),
    TripStatus.COMPLETED to emptySet(),
    TripStatus.CANCELLED to emptySet(),
)

fun TripStatus.canTransitionTo(next: TripStatus): Boolean =
    next in tripTransitions.getValue(this)

/** The single forward step a driver takes from the current status, if any. */
fun TripStatus.nextDriverStatus(): TripStatus? = when (this) {
    TripStatus.ASSIGNED -> TripStatus.ON_WAY
    TripStatus.ON_WAY -> TripStatus.ARRIVED_PICKUP
    TripStatus.ARRIVED_PICKUP -> TripStatus.IN_PROGRESS
    TripStatus.IN_PROGRESS -> TripStatus.COMPLETED
    TripStatus.SEARCHING, TripStatus.COMPLETED, TripStatus.CANCELLED -> null
}
