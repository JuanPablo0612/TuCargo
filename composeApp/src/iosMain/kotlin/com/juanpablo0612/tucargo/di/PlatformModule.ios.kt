package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.location.IosLocationProvider
import com.juanpablo0612.tucargo.core.location.LocationProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<LocationProvider> { IosLocationProvider() }
}
