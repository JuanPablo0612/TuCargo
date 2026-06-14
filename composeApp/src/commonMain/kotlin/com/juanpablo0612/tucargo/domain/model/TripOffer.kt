package com.juanpablo0612.tucargo.domain.model

data class TripOffer(
    val id: String = "",
    val tripId: String = "",
    val driverId: String = "",
    val attempt: Int = 0,
    val sentAt: Long = 0L,
    val expiresAt: Long = 0L,
    val response: OfferResponse = OfferResponse.PENDING,
    val totalPrice: Cop = Cop(0),
    val commissionFee: Cop = Cop(0),
    val distanceKm: Double = 0.0,
    val originAddr: String = "",
    val destAddr: String = ""
)
