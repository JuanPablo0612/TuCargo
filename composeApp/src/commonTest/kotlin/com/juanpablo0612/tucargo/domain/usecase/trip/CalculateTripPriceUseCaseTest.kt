package com.juanpablo0612.tucargo.domain.usecase.trip

import com.juanpablo0612.tucargo.data.config.SystemConfig
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.testutil.FakeConfigRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateTripPriceUseCaseTest {

    private val configRepository = FakeConfigRepository()
    private val useCase = CalculateTripPriceUseCase(configRepository)

    @Test
    fun zeroDistance_fails() = runTest {
        val result = useCase(4.6, -74.0, 4.6, -74.0)
        assertTrue(result.exceptionOrNull() is AppError.Validation.InvalidTrip)
    }

    @Test
    fun distanceWithinIncludedKm_chargesBaseFareOnly() = runTest {
        configRepository.configResult = Result.success(
            SystemConfig(basePrice = 1000.0, baseKmIncluded = 50.0, pricePerKm = 100.0, commissionPercentage = 0.1)
        )

        // ~2.2 km apart, well below the 50 km included.
        val quote = useCase(0.0, 0.0, 0.0, 0.02).getOrThrow()

        assertEquals(0.0, quote.priceDistance)
        assertEquals(1000.0, quote.priceTotal)
    }

    @Test
    fun quote_isInternallyConsistent() = runTest {
        configRepository.configResult = Result.success(
            SystemConfig(basePrice = 1000.0, baseKmIncluded = 1.0, pricePerKm = 100.0, commissionPercentage = 0.1)
        )

        val quote = useCase(0.0, 0.0, 0.0, 0.05).getOrThrow()

        assertTrue(quote.distanceKm > 1.0)
        assertTrue(quote.priceDistance > 0.0)
        assertEquals(quote.priceBase + quote.priceDistance, quote.priceTotal)
        assertEquals(quote.priceTotal * 0.1, quote.commissionFee)
    }

    @Test
    fun missingConfig_propagatesFailure() = runTest {
        configRepository.configResult =
            Result.failure(AppError.DataCorruption("missing config"))

        val result = useCase(0.0, 0.0, 0.0, 0.05)

        assertTrue(result.exceptionOrNull() is AppError.DataCorruption)
    }
}
