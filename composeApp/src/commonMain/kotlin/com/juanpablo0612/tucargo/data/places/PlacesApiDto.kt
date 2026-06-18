package com.juanpablo0612.tucargo.data.places

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Google Places API (New) Autocomplete ---

@Serializable
data class AutocompleteRequestDto(
    val input: String,
    @SerialName("locationBias") val locationBias: LocationBiasDto? = null,
    @SerialName("languageCode") val languageCode: String = "es",
    @SerialName("includedRegionCodes") val includedRegionCodes: List<String> = listOf("co"),
    @SerialName("sessionToken") val sessionToken: String? = null
)

@Serializable
data class LocationBiasDto(
    val circle: CircleDto? = null
)

@Serializable
data class CircleDto(
    val center: LatLngDto,
    val radius: Double = 50000.0
)

@Serializable
data class LatLngDto(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class AutocompleteResponseDto(
    val suggestions: List<SuggestionDto> = emptyList()
)

@Serializable
data class SuggestionDto(
    val placePrediction: PlacePredictionDto? = null
)

@Serializable
data class PlacePredictionDto(
    @SerialName("placeId") val placeId: String = "",
    val text: TextDto? = null,
    val structuredFormat: StructuredFormatDto? = null
)

@Serializable
data class TextDto(
    val text: String = ""
)

@Serializable
data class StructuredFormatDto(
    val mainText: TextDto? = null,
    val secondaryText: TextDto? = null
)

// --- Google Places API (New) Place Details ---

@Serializable
data class PlaceDetailsResponseDto(
    val formattedAddress: String = "",
    val location: LatLngDto? = null
)

// --- Google Geocoding API ---

@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto> = emptyList(),
    val status: String = ""
)

@Serializable
data class GeocodingResultDto(
    @SerialName("formatted_address") val formattedAddress: String = "",
    @SerialName("place_id") val placeId: String = "",
    val geometry: GeometryDto? = null
)

@Serializable
data class GeometryDto(
    val location: GeocodingLocationDto? = null
)

@Serializable
data class GeocodingLocationDto(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
