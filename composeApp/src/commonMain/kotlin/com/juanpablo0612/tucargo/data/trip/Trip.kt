// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/data/trip/Trip.kt
// NOTA: Renombra la carpeta "shipment" a "trip" en el explorador de archivos

package com.juanpablo0612.tucargo.data.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String = "",
    val status: TripStatus = TripStatus.SEARCHING,
    @SerialName("created_at")
    val createdAt: Long = 0L,
    @SerialName("completed_at")
    val completedAt: Long? = null,

    // Actores
    @SerialName("client_id")
    val clientId: String = "",
    @SerialName("driver_id")
    val driverId: String? = null,
    @SerialName("driver_name")
    val driverName: String = "",
    @SerialName("driver_plate")
    val driverPlate: String = "",

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
    val origin: TripLocation = TripLocation(),
    val destination: TripLocation = TripLocation(),
    @SerialName("distance_km")
    val distanceKm: Double = 0.0,
    @SerialName("cargo_description")
    val cargoDescription: String = "",
    @SerialName("delivery_code")
    val deliveryCode: String = ""
)

@Serializable
data class TripLocation(
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

@Serializable
enum class TripStatus {
    SEARCHING,
    ASSIGNED,
    ON_WAY,
    ARRIVED_PICKUP,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    fun displayName(): String = when (this) {
        SEARCHING -> "Buscando conductor"
        ASSIGNED -> "Conductor asignado"
        ON_WAY -> "En camino"
        ARRIVED_PICKUP -> "Llegó al origen"
        IN_PROGRESS -> "En progreso"
        COMPLETED -> "Completado"
        CANCELLED -> "Cancelado"
    }

    fun displayColor(): String = when (this) {
        SEARCHING -> "secondary"
        ASSIGNED, ON_WAY, ARRIVED_PICKUP -> "primary"
        IN_PROGRESS -> "tertiary"
        COMPLETED -> "success"
        CANCELLED -> "error"
    }
}
