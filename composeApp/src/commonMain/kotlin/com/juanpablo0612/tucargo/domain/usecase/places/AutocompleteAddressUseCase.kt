package com.juanpablo0612.tucargo.domain.usecase.places

import com.juanpablo0612.tucargo.data.places.PlacesRepository
import com.juanpablo0612.tucargo.domain.model.PlacePrediction

class AutocompleteAddressUseCase(private val placesRepository: PlacesRepository) {
    suspend operator fun invoke(
        query: String,
        sessionToken: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<List<PlacePrediction>> {
        if (query.length < 3) return Result.success(emptyList())
        return placesRepository.autocomplete(query, sessionToken, latitude, longitude)
    }
}
