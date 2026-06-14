package com.juanpablo0612.tucargo.domain.model

data class QuoteResult(
    val id: String,
    val distanceKm: Double,
    val polyline: String,
    val baseFare: Cop,
    val perKmFare: Cop,
    val totalPrice: Cop,
    val commissionFee: Cop,
    val originLat: Double,
    val originLng: Double,
    val originAddr: String,
    val destLat: Double,
    val destLng: Double,
    val destAddr: String,
    val validUntil: Long
)
