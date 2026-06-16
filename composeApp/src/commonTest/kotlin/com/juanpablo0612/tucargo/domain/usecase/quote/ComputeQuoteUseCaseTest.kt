package com.juanpablo0612.tucargo.domain.usecase.quote

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.PricingConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComputeQuoteUseCaseTest {

    // Standard config: baseFare=35000, perKmFare=5000, commissionRate=10
    private val standardConfig = PricingConfig(
        baseFare = Cop(35000),
        perKmFare = Cop(5000),
        commissionRate = 10
    )
    private val useCase = ComputeQuoteUseCase()

    private fun compute(distanceKm: Double) =
        useCase(ComputeQuoteUseCase.Input(distanceKm, standardConfig)).getOrThrow()

    // --- Reference table (all 7 rows must match exactly) ---

    @Test
    fun distance_0_4km_givesMinimumFare() {
        val out = compute(0.4)
        assertEquals(35000, out.totalPrice.amount)
        assertEquals(3500, out.commissionFee.amount)
    }

    @Test
    fun distance_1_0km_givesMinimumFare() {
        val out = compute(1.0)
        assertEquals(35000, out.totalPrice.amount)
        assertEquals(3500, out.commissionFee.amount)
    }

    @Test
    fun distance_1_7km_givesCorrectPrice() {
        val out = compute(1.7)
        assertEquals(38500, out.totalPrice.amount)
        assertEquals(3900, out.commissionFee.amount)
    }

    @Test
    fun distance_4_0km_givesCorrectPrice() {
        val out = compute(4.0)
        assertEquals(50000, out.totalPrice.amount)
        assertEquals(5000, out.commissionFee.amount)
    }

    @Test
    fun distance_6_4km_givesCorrectPrice() {
        val out = compute(6.4)
        assertEquals(62000, out.totalPrice.amount)
        assertEquals(6200, out.commissionFee.amount)
    }

    @Test
    fun distance_12_0km_givesCorrectPrice() {
        val out = compute(12.0)
        assertEquals(90000, out.totalPrice.amount)
        assertEquals(9000, out.commissionFee.amount)
    }

    @Test
    fun distance_25_0km_givesCorrectPrice() {
        val out = compute(25.0)
        assertEquals(155000, out.totalPrice.amount)
        assertEquals(15500, out.commissionFee.amount)
    }

    // --- Edge / rejection cases ---

    @Test
    fun negativeDistance_returnsFailure() {
        val result = useCase(ComputeQuoteUseCase.Input(-1.0, standardConfig))
        assertTrue(result.exceptionOrNull() is AppError.Validation.InvalidTrip)
    }

    @Test
    fun nanDistance_returnsFailure() {
        val result = useCase(ComputeQuoteUseCase.Input(Double.NaN, standardConfig))
        assertTrue(result.exceptionOrNull() is AppError.Validation.InvalidTrip)
    }

    @Test
    fun infiniteDistance_returnsFailure() {
        val result = useCase(ComputeQuoteUseCase.Input(Double.POSITIVE_INFINITY, standardConfig))
        assertTrue(result.exceptionOrNull() is AppError.Validation.InvalidTrip)
    }

    @Test
    fun distanceOver60km_returnsQuoteOutOfRange() {
        val result = useCase(ComputeQuoteUseCase.Input(60.1, standardConfig))
        assertTrue(result.exceptionOrNull() is AppError.Validation.QuoteOutOfRange)
    }

    @Test
    fun distanceExactly60km_succeeds() {
        val result = useCase(ComputeQuoteUseCase.Input(60.0, standardConfig))
        assertTrue(result.isSuccess)
    }
}
