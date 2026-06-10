package com.juanpablo0612.tucargo.core.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosLocationProvider : LocationProvider {
    override fun getLocations(): Flow<LocationUpdate> = emptyFlow()
    override suspend fun getCurrentLocation(): LocationUpdate? = null
}
