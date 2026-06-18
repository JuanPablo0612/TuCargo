package com.juanpablo0612.tucargo.data.places

import com.juanpablo0612.tucargo.domain.model.PlaceDetails
import com.juanpablo0612.tucargo.domain.model.PlacePrediction

fun PlacePredictionDto.toDomain(): PlacePrediction = PlacePrediction(
    placeId = placeId,
    mainText = structuredFormat?.mainText?.text ?: text?.text ?: "",
    secondaryText = structuredFormat?.secondaryText?.text ?: "",
    fullText = text?.text ?: "${structuredFormat?.mainText?.text}, ${structuredFormat?.secondaryText?.text}"
)

fun PlaceDetailsResponseDto.toDomain(placeId: String): PlaceDetails = PlaceDetails(
    placeId = placeId,
    formattedAddress = formattedAddress,
    latitude = location?.latitude ?: 0.0,
    longitude = location?.longitude ?: 0.0
)

fun GeocodingResultDto.toDomain(): PlaceDetails = PlaceDetails(
    placeId = placeId,
    formattedAddress = formattedAddress,
    latitude = geometry?.location?.lat ?: 0.0,
    longitude = geometry?.location?.lng ?: 0.0
)
