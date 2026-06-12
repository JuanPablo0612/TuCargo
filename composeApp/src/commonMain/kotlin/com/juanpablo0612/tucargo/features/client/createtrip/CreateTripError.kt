package com.juanpablo0612.tucargo.features.client.createtrip

sealed interface CreateTripError {
    data object QuoteError : CreateTripError
    data object SubmitError : CreateTripError
    data object InvalidTrip : CreateTripError
    data object UserNotAuthenticated : CreateTripError
}
