package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.location.AndroidLocationProvider
import com.juanpablo0612.tucargo.core.location.LocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
}
