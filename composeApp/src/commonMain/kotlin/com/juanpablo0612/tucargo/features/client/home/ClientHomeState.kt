package com.juanpablo0612.tucargo.features.client.home

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ClientHomeState(
    val isLoading: Boolean = false,
    val isLoadingTrips: Boolean = false,
    val user: User? = null,
    val recentTrips: ImmutableList<Trip> = persistentListOf(),
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val error: ClientHomeError? = null
)
