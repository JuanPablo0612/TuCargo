package com.juanpablo0612.tucargo.domain.trip

import com.juanpablo0612.tucargo.domain.model.TripStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripTransitionsTest {

    @Test
    fun matrix_coversEveryStatus() {
        assertEquals(TripStatus.entries.toSet(), tripTransitions.keys)
    }

    @Test
    fun validTransitions_areAllowed() {
        assertTrue(TripStatus.REQUESTED.canTransitionTo(TripStatus.OFFERED))
        assertTrue(TripStatus.REQUESTED.canTransitionTo(TripStatus.CANCELLED_CLIENT))
        assertTrue(TripStatus.OFFERED.canTransitionTo(TripStatus.ACCEPTED))
        assertTrue(TripStatus.OFFERED.canTransitionTo(TripStatus.CANCELLED_CLIENT))
        assertTrue(TripStatus.OFFERED.canTransitionTo(TripStatus.CANCELLED_NO_DRIVER))
        assertTrue(TripStatus.ACCEPTED.canTransitionTo(TripStatus.AT_PICKUP))
        assertTrue(TripStatus.ACCEPTED.canTransitionTo(TripStatus.CANCELLED_DRIVER))
        assertTrue(TripStatus.AT_PICKUP.canTransitionTo(TripStatus.IN_TRANSIT))
        assertTrue(TripStatus.IN_TRANSIT.canTransitionTo(TripStatus.AT_DROPOFF))
        assertTrue(TripStatus.AT_DROPOFF.canTransitionTo(TripStatus.COMPLETED))
    }

    @Test
    fun invalidTransitions_areRejected() {
        assertFalse(TripStatus.REQUESTED.canTransitionTo(TripStatus.AT_PICKUP))
        assertFalse(TripStatus.REQUESTED.canTransitionTo(TripStatus.COMPLETED))
        assertFalse(TripStatus.AT_PICKUP.canTransitionTo(TripStatus.CANCELLED_CLIENT))
        assertFalse(TripStatus.AT_DROPOFF.canTransitionTo(TripStatus.CANCELLED_CLIENT))
        assertFalse(TripStatus.COMPLETED.canTransitionTo(TripStatus.REQUESTED))
        assertFalse(TripStatus.CANCELLED_CLIENT.canTransitionTo(TripStatus.ACCEPTED))
    }

    @Test
    fun terminalStatuses_haveNoTransitions() {
        assertTrue(tripTransitions.getValue(TripStatus.COMPLETED).isEmpty())
        assertTrue(tripTransitions.getValue(TripStatus.CANCELLED_CLIENT).isEmpty())
        assertTrue(tripTransitions.getValue(TripStatus.CANCELLED_NO_DRIVER).isEmpty())
        assertTrue(tripTransitions.getValue(TripStatus.CANCELLED_DRIVER).isEmpty())
        assertTrue(tripTransitions.getValue(TripStatus.CANCELLED_ADMIN).isEmpty())
    }

    @Test
    fun nextDriverStatus_followsTheLifecycle() {
        assertEquals(TripStatus.AT_PICKUP, TripStatus.ACCEPTED.nextDriverStatus())
        assertEquals(TripStatus.IN_TRANSIT, TripStatus.AT_PICKUP.nextDriverStatus())
        assertEquals(TripStatus.AT_DROPOFF, TripStatus.IN_TRANSIT.nextDriverStatus())
        assertEquals(TripStatus.COMPLETED, TripStatus.AT_DROPOFF.nextDriverStatus())
        assertEquals(null, TripStatus.REQUESTED.nextDriverStatus())
        assertEquals(null, TripStatus.COMPLETED.nextDriverStatus())
        assertEquals(null, TripStatus.CANCELLED_CLIENT.nextDriverStatus())
    }
}
