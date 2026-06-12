package com.juanpablo0612.tucargo.domain.usecase

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
        val trip = Trip(id = "t1", status = TripStatus.IN_PROGRESS)

        val result = useCase(trip)

        assertTrue(result.exceptionOrNull() is AppError.Trip.InvalidTransition)
        assertNull(tripRepository.lastStatusUpdate)
    }

    @Test
    fun cancellingWhileSearching_succeeds() = runTest {
        val trip = Trip(id = "t1", status = TripStatus.SEARCHING)

        val result = useCase(trip)

        assertTrue(result.isSuccess)
        assertEquals(
            Triple("t1", TripStatus.SEARCHING, TripStatus.CANCELLED),
            tripRepository.lastStatusUpdate
        )
    }
}
