package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.location.AndroidLocationProvider
import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.service.LocationServiceController
import com.juanpablo0612.tucargo.data.tracking.LocationBuffer
import com.juanpablo0612.tucargo.data.tracking.RoomLocationBuffer
import com.juanpablo0612.tucargo.data.tracking.room.LocationDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
    single { LocationDatabase.build(androidContext()) }
    single { get<LocationDatabase>().locationDao() }
    single<LocationBuffer> { RoomLocationBuffer(get()) }
    single { LocationServiceController(androidContext()) }
}
