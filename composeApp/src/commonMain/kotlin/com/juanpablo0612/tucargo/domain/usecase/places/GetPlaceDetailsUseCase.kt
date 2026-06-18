package com.juanpablo0612.tucargo.domain.usecase.places

import com.juanpablo0612.tucargo.data.places.PlacesRepository
import com.juanpablo0612.tucargo.domain.model.PlaceDetails

class GetPlaceDetailsUseCase(private val placesRepository: PlacesRepository) {
    suspend operator fun invoke(
        placeId: String,
        sessionToken: String
    ): Result<PlaceDetails> = placesRepository.getPlaceDetails(placeId, sessionToken)
}
