package com.juanpablo0612.tucargo.data.places

import com.juanpablo0612.tucargo.BuildKonfig
import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.PlaceDetails
import com.juanpablo0612.tucargo.domain.model.PlacePrediction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.withContext

private const val DEFAULT_LAT = 4.6097
private const val DEFAULT_LNG = -74.0817
private const val AUTOCOMPLETE_URL = "https://places.googleapis.com/v1/places:autocomplete"
private const val PLACE_DETAILS_URL = "https://places.googleapis.com/v1/places/"
private const val GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json"

class PlacesRepositoryImpl(
    private val httpClient: HttpClient,
    private val dispatchers: AppDispatchers
) : PlacesRepository {

    private val apiKey: String get() = BuildKonfig.GOOGLE_PLACES_API_KEY

    override suspend fun autocomplete(
        query: String,
        sessionToken: String,
        latitude: Double?,
        longitude: Double?
    ): Result<List<PlacePrediction>> = safeCall {
        withContext(dispatchers.io) {
            val biasLat = latitude ?: DEFAULT_LAT
            val biasLng = longitude ?: DEFAULT_LNG

            val request = AutocompleteRequestDto(
                input = query,
                locationBias = LocationBiasDto(
                    circle = CircleDto(
                        center = LatLngDto(biasLat, biasLng),
                        radius = 50000.0
                    )
                ),
                sessionToken = sessionToken
            )

            val response: AutocompleteResponseDto = httpClient.post(AUTOCOMPLETE_URL) {
                contentType(ContentType.Application.Json)
                header("X-Goog-Api-Key", apiKey)
                setBody(request)
            }.body()

            response.suggestions.mapNotNull { suggestion ->
                suggestion.placePrediction?.toDomain()
            }
        }
    }

    override suspend fun getPlaceDetails(
        placeId: String,
        sessionToken: String
    ): Result<PlaceDetails> = safeCall {
        withContext(dispatchers.io) {
            val response: PlaceDetailsResponseDto = httpClient.get("$PLACE_DETAILS_URL$placeId") {
                header("X-Goog-Api-Key", apiKey)
                header("X-Goog-FieldMask", "formattedAddress,location")
                parameter("sessionToken", sessionToken)
            }.body()

            if (response.location == null) throw AppError.Places.PlaceNotFound
            response.toDomain(placeId)
        }
    }

    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Result<PlaceDetails> = safeCall {
        withContext(dispatchers.io) {
            val response: GeocodingResponseDto = httpClient.get(GEOCODING_URL) {
                parameter("latlng", "$latitude,$longitude")
                parameter("key", apiKey)
                parameter("language", "es")
                parameter("result_type", "street_address|route|sublocality")
            }.body()

            val result = response.results.firstOrNull()
                ?: throw AppError.Places.GeocodingUnavailable
            result.toDomain()
        }
    }
}
