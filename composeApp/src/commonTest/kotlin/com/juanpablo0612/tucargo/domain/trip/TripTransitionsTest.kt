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
        assertTrue(TripStatus.ACCEPTED.canTransitionTo(TripStatus.ON_WAY))
        assertTrue(TripStatus.ACCEPTED.canTransitionTo(TripStatus.CANCELLED_DRIVER))
        assertTrue(TripStatus.ON_WAY.canTransitionTo(TripStatus.ARRIVED_PICKUP))
        assertTrue(TripStatus.ARRIVED_PICKUP.canTransitionTo(TripStatus.IN_PROGRESS))
        assertTrue(TripStatus.IN_PROGRESS.canTransitionTo(TripStatus.COMPLETED))
    }

    @Test
    fun invalidTransitions_areRejected() {
        assertFalse(TripStatus.REQUESTED.canTransitionTo(TripStatus.ON_WAY))
        assertFalse(TripStatus.REQUESTED.canTransitionTo(TripStatus.COMPLETED))
        assertFalse(TripStatus.ON_WAY.canTransitionTo(TripStatus.CANCELLED_CLIENT))
        assertFalse(TripStatus.IN_PROGRESS.canTransitionTo(TripStatus.CANCELLED_CLIENT))
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
        assertEquals(TripStatus.ON_WAY, TripStatus.ACCEPTED.nextDriverStatus())
        assertEquals(TripStatus.ARRIVED_PICKUP, TripStatus.ON_WAY.nextDriverStatus())
        assertEquals(TripStatus.IN_PROGRESS, TripStatus.ARRIVED_PICKUP.nextDriverStatus())
        assertEquals(TripStatus.COMPLETED, TripStatus.IN_PROGRESS.nextDriverStatus())
        assertEquals(null, TripStatus.REQUESTED.nextDriverStatus())
        assertEquals(null, TripStatus.COMPLETED.nextDriverStatus())
        assertEquals(null, TripStatus.CANCELLED_CLIENT.nextDriverStatus())
    }
}
