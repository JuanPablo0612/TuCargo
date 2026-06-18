package com.juanpablo0612.tucargo.domain.usecase.places

import com.juanpablo0612.tucargo.data.places.PlacesRepository
import com.juanpablo0612.tucargo.domain.model.PlaceDetails

class ReverseGeocodeUseCase(private val placesRepository: PlacesRepository) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Result<PlaceDetails> = placesRepository.reverseGeocode(latitude, longitude)
}
