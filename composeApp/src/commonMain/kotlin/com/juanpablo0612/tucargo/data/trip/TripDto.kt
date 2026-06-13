package com.juanpablo0612.tucargo.data.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripDto(
    val id: String = "",
    val status: String = "SEARCHING",
    @SerialName("created_at")
    val createdAt: Long = 0L,
    @SerialName("completed_at")
    val completedAt: Long? = null,

    // Actores
    @SerialName("client_id")
    val clientId: String = "",
    @SerialName("client_name")
    val clientName: String = "",
    @SerialName("client_phone")
    val clientPhone: String = "",
    @SerialName("driver_id")
    val driverId: String? = null,
    @SerialName("driver_name")
    val driverName: String = "",
    @SerialName("driver_plate")
    val driverPlate: String = "",
    @SerialName("driver_last_lat")
    val driverLastLat: Double? = null,
    @SerialName("driver_last_lng")
    val driverLastLng: Double? = null,

    // Economía
    @SerialName("price_total")
    val priceTotal: Double = 0.0,
    @SerialName("price_base")
    val priceBase: Double = 0.0,
    @SerialName("price_distance")
    val priceDistance: Double = 0.0,
    @SerialName("commission_fee")
    val commissionFee: Double = 0.0,
    @SerialName("payment_method")
    val paymentMethod: String = "CASH",

    // Logística
    val origin: TripLocationDto = TripLocationDto(),
    val destination: TripLocationDto = TripLocationDto(),
    @SerialName("distance_km")
    val distanceKm: Double = 0.0,
    @SerialName("cargo_description")
    val cargoDescription: String = "",
    @SerialName("delivery_code")
    val deliveryCode: String = ""
)

@Serializable
data class TripLocationDto(
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
