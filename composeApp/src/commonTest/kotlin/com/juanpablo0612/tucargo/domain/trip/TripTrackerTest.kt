package com.juanpablo0612.tucargo.domain.trip

import com.juanpablo0612.tucargo.core.location.LocationUpdate
import com.juanpablo0612.tucargo.testutil.FakeLocationProvider
import com.juanpablo0612.tucargo.testutil.FakeTripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TripTrackerTest {

    @Test
    fun tracker_starts_and_updates_location() = runTest {
        val repository = FakeTripRepository()
        val locationProvider = FakeLocationProvider()
        val tracker = TripTracker(repository, locationProvider, backgroundScope)
        
        tracker.startTracking("trip1", intervalMillis = 1)
        
        locationProvider.locations.value = LocationUpdate(1.0, 1.0)
        
        assertTrue(tracker.state.value is TrackingState.Tracking)
        assertEquals("trip1", (tracker.state.value as TrackingState.Tracking).tripId)
        
        tracker.stopTracking()
        assertEquals(TrackingState.Idle, tracker.state.value)
    }
}
