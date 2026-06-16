package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.testutil.FakeTripRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CancelTripUseCaseTest {

    private val tripRepository = FakeTripRepository()
    private val useCase = CancelTripUseCase(tripRepository)

    @Test
    fun cancellingAfterPickup_failsWithoutRepositoryCall() = runTest {
        val trip = Trip(id = "t1", status = TripStatus.AT_DROPOFF)

        val result = useCase(trip)

        assertTrue(result.exceptionOrNull() is AppError.Trip.InvalidTransition)
        assertNull(tripRepository.lastStatusUpdate)
    }

    @Test
    fun cancellingWhileRequested_succeeds() = runTest {
        val trip = Trip(id = "t1", status = TripStatus.REQUESTED)

        val result = useCase(trip)

        assertTrue(result.isSuccess)
        assertEquals(
            Triple("t1", TripStatus.REQUESTED, TripStatus.CANCELLED_CLIENT),
            tripRepository.lastStatusUpdate
        )
    }

    @Test
    fun cancellingWhileOffered_succeeds() = runTest {
        val trip = Trip(id = "t1", status = TripStatus.OFFERED)

        val result = useCase(trip)

        assertTrue(result.isSuccess)
        assertEquals(
            Triple("t1", TripStatus.OFFERED, TripStatus.CANCELLED_CLIENT),
            tripRepository.lastStatusUpdate
        )
    }
}
