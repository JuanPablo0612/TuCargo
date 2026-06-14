package com.juanpablo0612.tucargo.core.fcm

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object OfferEventBus {
    private val _incomingOffers = MutableSharedFlow<Map<String, String>>(extraBufferCapacity = 1)
    val incomingOffers: SharedFlow<Map<String, String>> = _incomingOffers.asSharedFlow()

    private val _cancelledOffers = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val cancelledOffers: SharedFlow<String> = _cancelledOffers.asSharedFlow()

    fun postOffer(data: Map<String, String>) {
        _incomingOffers.tryEmit(data)
    }

    fun postCancellation(tripId: String) {
        _cancelledOffers.tryEmit(tripId)
    }
}
