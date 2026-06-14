package com.juanpablo0612.tucargo.features.driver.home.presentation

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.trip.TrackingState
import com.juanpablo0612.tucargo.domain.trip.TripTracker
import com.juanpablo0612.tucargo.domain.usecase.AcceptOfferUseCase
import com.juanpablo0612.tucargo.domain.usecase.AcceptTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveAvailableTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveDriverActiveTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.RejectOfferUseCase
import com.juanpablo0612.tucargo.domain.usecase.UpdateDriverStatusUseCase
import com.juanpablo0612.tucargo.testutil.FakeLocationProvider
import com.juanpablo0612.tucargo.testutil.FakeTripRepository
import com.juanpablo0612.tucargo.testutil.FakeUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DriverHomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var trackerScope: CoroutineScope
    private lateinit var tripRepository: FakeTripRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var tracker: TripTracker
    private lateinit var viewModel: DriverHomeViewModel

    private val verifiedDriver = User(
        id = "d1",
        role = UserRole.DRIVER,
        isVerified = true,
        isOnline = true
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        trackerScope = CoroutineScope(SupervisorJob() + testDispatcher)
        tripRepository = FakeTripRepository()
        userRepository = FakeUserRepository().apply {
            currentUser = Result.success(verifiedDriver)
            currentUserId = verifiedDriver.id
        }
        tracker = TripTracker(tripRepository, FakeLocationProvider(), trackerScope)
        viewModel = DriverHomeViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(userRepository),
            getCurrentUserIdUseCase = GetCurrentUserIdUseCase(userRepository),
            updateDriverStatusUseCase = UpdateDriverStatusUseCase(userRepository),
            observeDriverActiveTripsUseCase = ObserveDriverActiveTripsUseCase(tripRepository),
            observeAvailableTripsUseCase = ObserveAvailableTripsUseCase(tripRepository),
            acceptTripUseCase = AcceptTripUseCase(tripRepository, userRepository),
            acceptOfferUseCase = AcceptOfferUseCase(tripRepository),
            rejectOfferUseCase = RejectOfferUseCase(tripRepository),
            tripTracker = tracker
        )
    }

    @AfterTest
    fun tearDown() {
        trackerScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun trackingWaitsForLocationPermission() = runTest {
        tripRepository.activeTripsFlow.value =
            listOf(Trip(id = "t1", status = TripStatus.ON_WAY))
        testDispatcher.scheduler.advanceUntilIdle()

        // Without permission the tracker must not start (it used to crash
        // the application scope with a SecurityException).
        assertTrue(tracker.state.value is TrackingState.Idle)

        // startTracking sets the state synchronously; advancing the
        // scheduler here would spin forever on the tracker's sample ticker.
        viewModel.onAction(DriverHomeAction.LocationPermissionResult(granted = true))

        assertEquals(TrackingState.Tracking("t1"), tracker.state.value)
        tracker.stopTracking()
    }

    @Test
    fun deniedPermission_surfacesBanner() = runTest {
        viewModel.onAction(DriverHomeAction.LocationPermissionResult(granted = false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DriverHomeError.LocationPermissionDenied, viewModel.uiState.value.error)
        assertTrue(tracker.state.value is TrackingState.Idle)
    }

    @Test
    fun availableTrips_areCollectedWhileOnline() = runTest {
        tripRepository.availableTripsFlow.value =
            listOf(Trip(id = "t2", status = TripStatus.REQUESTED))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.availableTrips.size)

        viewModel.onAction(DriverHomeAction.ToggleAvailability(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.availableTrips.isEmpty())
    }

    @Test
    fun lostAcceptRace_showsTripTakenError() = runTest {
        tripRepository.acceptTripResult = Result.failure(AppError.Trip.AlreadyTaken)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(DriverHomeAction.AcceptTrip("t9"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DriverHomeError.TripAlreadyTaken, viewModel.uiState.value.error)
    }
}
