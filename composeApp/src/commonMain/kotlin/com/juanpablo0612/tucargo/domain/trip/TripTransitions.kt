package com.juanpablo0612.tucargo.domain.trip

import com.juanpablo0612.tucargo.domain.model.TripStatus

/**
 * Single source of truth for the trip lifecycle, mirrored exactly by the
 * `update` rules on /trips in firestore.rules. Keep both in sync.
 */
val tripTransitions: Map<TripStatus, Set<TripStatus>> = mapOf(
    TripStatus.REQUESTED to setOf(TripStatus.OFFERED, TripStatus.CANCELLED_CLIENT, TripStatus.CANCELLED_NO_DRIVER),
    TripStatus.OFFERED to setOf(TripStatus.ACCEPTED, TripStatus.CANCELLED_CLIENT, TripStatus.CANCELLED_NO_DRIVER),
    TripStatus.ACCEPTED to setOf(TripStatus.ON_WAY, TripStatus.CANCELLED_DRIVER),
    TripStatus.ON_WAY to setOf(TripStatus.ARRIVED_PICKUP),
    TripStatus.ARRIVED_PICKUP to setOf(TripStatus.IN_PROGRESS),
    TripStatus.IN_PROGRESS to setOf(TripStatus.COMPLETED),
    TripStatus.COMPLETED to emptySet(),
    TripStatus.CANCELLED_NO_DRIVER to emptySet(),
    TripStatus.CANCELLED_CLIENT to emptySet(),
    TripStatus.CANCELLED_DRIVER to emptySet(),
    TripStatus.CANCELLED_ADMIN to emptySet(),
)

fun TripStatus.canTransitionTo(next: TripStatus): Boolean =
    next in (tripTransitions[this] ?: emptySet())

/** The single forward step a driver takes from the current status, if any. */
fun TripStatus.nextDriverStatus(): TripStatus? = when (this) {
    TripStatus.ACCEPTED -> TripStatus.ON_WAY
    TripStatus.ON_WAY -> TripStatus.ARRIVED_PICKUP
    TripStatus.ARRIVED_PICKUP -> TripStatus.IN_PROGRESS
    TripStatus.IN_PROGRESS -> TripStatus.COMPLETED
    else -> null
}

fun TripStatus.isCancelled(): Boolean = this in setOf(
    TripStatus.CANCELLED_NO_DRIVER,
    TripStatus.CANCELLED_CLIENT,
    TripStatus.CANCELLED_DRIVER,
    TripStatus.CANCELLED_ADMIN
)
