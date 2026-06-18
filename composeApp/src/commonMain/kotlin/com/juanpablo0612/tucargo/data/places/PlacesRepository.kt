package com.juanpablo0612.tucargo.data.places

import com.juanpablo0612.tucargo.domain.model.PlaceDetails
import com.juanpablo0612.tucargo.domain.model.PlacePrediction

interface PlacesRepository {
    suspend fun autocomplete(
        query: String,
        sessionToken: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<List<PlacePrediction>>

    suspend fun getPlaceDetails(
        placeId: String,
        sessionToken: String
    ): Result<PlaceDetails>

    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Result<PlaceDetails>
}
