package com.juanpablo0612.tucargo.features.client.createtrip

import com.juanpablo0612.tucargo.domain.model.PaymentMethod

sealed interface CreateTripAction {
    data class OnMapClick(val latitude: Double, val longitude: Double) : CreateTripAction
    data object NextStep : CreateTripAction
    data object PreviousStep : CreateTripAction
    data class SelectPaymentMethod(val method: PaymentMethod) : CreateTripAction
    data object Submit : CreateTripAction
}
