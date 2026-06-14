package com.juanpablo0612.tucargo.data.quote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteDto(
    val id: String = "",
    @SerialName("distance_km")
    val distanceKm: Double = 0.0,
    val polyline: String = "",
    @SerialName("base_fare")
    val baseFare: Int = 0,
    @SerialName("per_km_fare")
    val perKmFare: Int = 0,
    @SerialName("total_price")
    val totalPrice: Int = 0,
    @SerialName("commission_fee")
    val commissionFee: Int = 0,
    @SerialName("origin_lat")
    val originLat: Double = 0.0,
    @SerialName("origin_lng")
    val originLng: Double = 0.0,
    @SerialName("origin_addr")
    val originAddr: String = "",
    @SerialName("dest_lat")
    val destLat: Double = 0.0,
    @SerialName("dest_lng")
    val destLng: Double = 0.0,
    @SerialName("dest_addr")
    val destAddr: String = "",
    @SerialName("valid_until")
    val validUntil: Long = 0L,
    val consumed: Boolean = false
)
