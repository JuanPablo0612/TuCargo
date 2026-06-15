package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.location.IosLocationProvider
import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.service.IosLocationServiceController
import com.juanpablo0612.tucargo.core.service.LocationServiceController
import com.juanpablo0612.tucargo.data.tracking.InMemoryLocationBuffer
import com.juanpablo0612.tucargo.data.tracking.LocationBuffer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<LocationProvider> { IosLocationProvider() }
    single<LocationBuffer> { InMemoryLocationBuffer() }
    single<LocationServiceController> { IosLocationServiceController() }
}
