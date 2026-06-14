package com.juanpablo0612.tucargo.data.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripOfferDto(
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String = "",
    @SerialName("driver_id")
    val driverId: String = "",
    val attempt: Int = 0,
    @SerialName("sent_at")
    val sentAt: Long = 0L,
    @SerialName("expires_at")
    val expiresAt: Long = 0L,
    val response: String = "PENDING",
    @SerialName("total_price")
    val totalPrice: Int = 0,
    @SerialName("commission_fee")
    val commissionFee: Int = 0,
    @SerialName("distance_km")
    val distanceKm: Double = 0.0,
    @SerialName("origin_addr")
    val originAddr: String = "",
    @SerialName("dest_addr")
    val destAddr: String = ""
)
