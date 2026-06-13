package com.juanpablo0612.tucargo.domain.model

data class Trip(
    val id: String = "",
    val status: TripStatus = TripStatus.SEARCHING,
    val createdAt: Long = 0L,
    val completedAt: Long? = null,
    val clientId: String = "",
    // Denormalized at creation so the driver's screens never need to read
    // another user's document (the users collection is closed by the rules).
    val clientName: String = "",
    val clientPhone: String = "",
    val driverId: String? = null,
    val driverName: String = "",
    val driverPlate: String = "",
    val driverLastLat: Double? = null,
    val driverLastLng: Double? = null,
    val priceTotal: Double = 0.0,
    val priceBase: Double = 0.0,
    val priceDistance: Double = 0.0,
    val commissionFee: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val origin: TripLocation = TripLocation(),
    val destination: TripLocation = TripLocation(),
    val distanceKm: Double = 0.0,
    val cargoDescription: String = "",
    val deliveryCode: String = ""
)

data class TripLocation(
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

enum class TripStatus {
    SEARCHING,
    ASSIGNED,
    ON_WAY,
    ARRIVED_PICKUP,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class PaymentMethod {
    CASH,
    WALLET
}
