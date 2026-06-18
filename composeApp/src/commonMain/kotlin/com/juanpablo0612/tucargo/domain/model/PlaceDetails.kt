package com.juanpablo0612.tucargo.domain.model

data class PlaceDetails(
    val placeId: String,
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double
)
