package com.juanpablo0612.tucargo.domain.model

data class TripQuote(
    val distanceKm: Double,
    val priceBase: Double,
    val priceDistance: Double,
    val priceTotal: Double,
    val commissionFee: Double
)
