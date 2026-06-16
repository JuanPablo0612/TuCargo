package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.testutil.FakeTripRepository
import com.juanpablo0612.tucargo.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AcceptTripUseCaseTest {

    private val tripRepository = FakeTripRepository()
    private val userRepository = FakeUserRepository()
    private val useCase = AcceptTripUseCase(tripRepository, userRepository)

    @Test
    fun unverifiedDriver_isRejectedWithoutTouchingTheTrip() = runTest {
        userRepository.currentUser = Result.success(
            User(id = "d1", role = UserRole.DRIVER, isVerified = false)
        )

        val result = useCase("trip-1")

        assertTrue(result.exceptionOrNull() is AppError.Trip.DriverNotVerified)
        assertNull(tripRepository.lastAcceptedTripId)
    }

    @Test
    fun clientRole_isRejected() = runTest {
        userRepository.currentUser = Result.success(
            User(id = "c1", role = UserRole.CLIENT, isVerified = true)
        )

        val result = useCase("trip-1")

        assertTrue(result.exceptionOrNull() is AppError.Trip.DriverNotVerified)
    }

    @Test
    fun lostRace_propagatesAlreadyTaken() = runTest {
        userRepository.currentUser = Result.success(
            User(id = "d1", role = UserRole.DRIVER, isVerified = true)
        )
        tripRepository.acceptTripResult = Result.failure(AppError.Trip.AlreadyTaken)

        val result = useCase("trip-1")

        assertTrue(result.exceptionOrNull() is AppError.Trip.AlreadyTaken)
    }

    @Test
    fun verifiedDriver_acceptsTheTrip() = runTest {
        userRepository.currentUser = Result.success(
            User(id = "d1", role = UserRole.DRIVER, isVerified = true)
        )

        val result = useCase("trip-1")

        assertTrue(result.isSuccess)
        assertEquals("trip-1", tripRepository.lastAcceptedTripId)
    }
}
