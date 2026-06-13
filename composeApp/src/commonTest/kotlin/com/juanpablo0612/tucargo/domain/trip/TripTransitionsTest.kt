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
        assertTrue(TripStatus.SEARCHING.canTransitionTo(TripStatus.ASSIGNED))
        assertTrue(TripStatus.SEARCHING.canTransitionTo(TripStatus.CANCELLED))
        assertTrue(TripStatus.ASSIGNED.canTransitionTo(TripStatus.ON_WAY))
        assertTrue(TripStatus.ASSIGNED.canTransitionTo(TripStatus.CANCELLED))
        assertTrue(TripStatus.ON_WAY.canTransitionTo(TripStatus.ARRIVED_PICKUP))
        assertTrue(TripStatus.ARRIVED_PICKUP.canTransitionTo(TripStatus.IN_PROGRESS))
        assertTrue(TripStatus.IN_PROGRESS.canTransitionTo(TripStatus.COMPLETED))
    }

    @Test
    fun invalidTransitions_areRejected() {
        assertFalse(TripStatus.SEARCHING.canTransitionTo(TripStatus.ON_WAY))
        assertFalse(TripStatus.SEARCHING.canTransitionTo(TripStatus.COMPLETED))
        assertFalse(TripStatus.ON_WAY.canTransitionTo(TripStatus.CANCELLED))
        assertFalse(TripStatus.IN_PROGRESS.canTransitionTo(TripStatus.CANCELLED))
        assertFalse(TripStatus.COMPLETED.canTransitionTo(TripStatus.SEARCHING))
        assertFalse(TripStatus.CANCELLED.canTransitionTo(TripStatus.ASSIGNED))
    }

    @Test
    fun terminalStatuses_haveNoTransitions() {
        assertTrue(tripTransitions.getValue(TripStatus.COMPLETED).isEmpty())
        assertTrue(tripTransitions.getValue(TripStatus.CANCELLED).isEmpty())
    }

    @Test
    fun nextDriverStatus_followsTheLifecycle() {
        assertEquals(TripStatus.ON_WAY, TripStatus.ASSIGNED.nextDriverStatus())
        assertEquals(TripStatus.ARRIVED_PICKUP, TripStatus.ON_WAY.nextDriverStatus())
        assertEquals(TripStatus.IN_PROGRESS, TripStatus.ARRIVED_PICKUP.nextDriverStatus())
        assertEquals(TripStatus.COMPLETED, TripStatus.IN_PROGRESS.nextDriverStatus())
        assertEquals(null, TripStatus.SEARCHING.nextDriverStatus())
        assertEquals(null, TripStatus.COMPLETED.nextDriverStatus())
        assertEquals(null, TripStatus.CANCELLED.nextDriverStatus())
    }
}
