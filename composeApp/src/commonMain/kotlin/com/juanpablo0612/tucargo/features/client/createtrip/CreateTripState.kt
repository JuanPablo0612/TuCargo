package com.juanpablo0612.tucargo.features.client.createtrip

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.domain.model.PaymentMethod
import com.juanpablo0612.tucargo.domain.model.TripQuote

enum class CreateTripStep { ORIGIN, DESTINATION, DETAILS }

@Immutable
data class CreateTripState(
    val step: CreateTripStep = CreateTripStep.ORIGIN,
    val originLat: Double? = null,
    val originLng: Double? = null,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null,
    val originAddressError: FieldError? = null,
    val destinationAddressError: FieldError? = null,
    val originPinMissing: Boolean = false,
    val destinationPinMissing: Boolean = false,
    val cargoDescriptionError: FieldError? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val quote: TripQuote? = null,
    val isQuoteLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: CreateTripError? = null,
    val createdTripId: String? = null
)
