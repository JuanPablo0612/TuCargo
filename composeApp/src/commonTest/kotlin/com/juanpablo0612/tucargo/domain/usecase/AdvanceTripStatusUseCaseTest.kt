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

class AdvanceTripStatusUseCaseTest {

    private val tripRepository = FakeTripRepository()
    private val useCase = AdvanceTripStatusUseCase(tripRepository)

    @Test
    fun illegalTransition_failsWithoutRepositoryCall() = runTest {
        val trip = Trip(id = "t1", status = TripStatus.REQUESTED)

        val result = useCase(trip, TripStatus.COMPLETED)

        assertTrue(result.exceptionOrNull() is AppError.Trip.InvalidTransition)
        assertNull(tripRepository.lastStatusUpdate)
    }

    @Test
    fun legalTransition_callsRepositoryWithExpectedStatuses() = runTest {
        val trip = Trip(id = "t1", status = TripStatus.ACCEPTED)

        val result = useCase(trip, TripStatus.AT_PICKUP)

        assertTrue(result.isSuccess)
        assertEquals(
            Triple("t1", TripStatus.ACCEPTED, TripStatus.AT_PICKUP),
            tripRepository.lastStatusUpdate
        )
    }
}
