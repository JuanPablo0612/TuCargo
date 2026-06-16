package com.juanpablo0612.tucargo.domain.usecase.quote

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.QuoteResult
import com.juanpablo0612.tucargo.testutil.FakeQuoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class RequestQuoteUseCaseTest {

    private val quoteRepository = FakeQuoteRepository()
    private val useCase = RequestQuoteUseCase(quoteRepository)

    private val validQuote = QuoteResult(
        id = "q1",
        distanceKm = 5.0,
        polyline = "",
        baseFare = Cop(35000),
        perKmFare = Cop(5000),
        totalPrice = Cop(55000),
        commissionFee = Cop(5500),
        originLat = 4.6,
        originLng = -74.0,
        originAddr = "Origen",
        destLat = 4.65,
        destLng = -74.05,
        destAddr = "Destino",
        validUntil = Long.MAX_VALUE
    )

    @Test
    fun sameOriginAndDest_returnsFailureWithoutCallingRepo() = runTest {
        val result = useCase(
            clientId = "c1",
            originLat = 4.6, originLng = -74.0, originAddr = "A",
            destLat = 4.6, destLng = -74.0, destAddr = "A"
        )

        assertTrue(result.exceptionOrNull() is AppError.Validation.SameOriginDest)
        assertTrue(quoteRepository.lastCreateQuoteCall == null)
    }

    @Test
    fun differentCoords_delegatesToRepository() = runTest {
        quoteRepository.createQuoteResult = Result.success(validQuote)

        val result = useCase(
            clientId = "c1",
            originLat = 4.6, originLng = -74.0, originAddr = "Origen",
            destLat = 4.65, destLng = -74.05, destAddr = "Destino"
        )

        assertTrue(result.isSuccess)
        assertTrue(quoteRepository.lastCreateQuoteCall != null)
    }

    @Test
    fun repoFailure_propagated() = runTest {
        quoteRepository.createQuoteResult = Result.failure(AppError.Validation.NoRoute)

        val result = useCase(
            clientId = "c1",
            originLat = 4.6, originLng = -74.0, originAddr = "Origen",
            destLat = 4.65, destLng = -74.05, destAddr = "Destino"
        )

        assertTrue(result.exceptionOrNull() is AppError.Validation.NoRoute)
    }
}
