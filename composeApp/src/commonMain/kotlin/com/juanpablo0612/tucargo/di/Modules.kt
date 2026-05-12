package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.location.MockLocationProvider
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.config.SystemConfig
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.trip.TripTrackingManager
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.features.auth.presentation.documents.DocumentViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.login.LoginViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.register.RegisterViewModel
import com.juanpablo0612.tucargo.features.client.home.ClientHomeViewModel
import com.juanpablo0612.tucargo.features.driver.home.presentation.DriverHomeViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    
    // Proveedor de ubicación (fácil de cambiar a RealLocationProvider después)
    single<LocationProvider> { MockLocationProvider() }
    
    // Configuración del sistema (Singleton)
    single { SystemConfig() }

    // Repositorios con inyección automática
    singleOf(::AuthRepository)
    singleOf(::UserRepository)
    singleOf(::TripRepository)
    
    // Scope global para procesos en segundo plano (como el tracking)
    single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    
    // Manager de tracking necesita el scope inyectado
    single { TripTrackingManager(get(), get(), get()) }
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::DocumentViewModel)
    viewModelOf(::ClientHomeViewModel)
    viewModelOf(::DriverHomeViewModel)
}

val appModule = module {
    includes(dataModule, viewModelModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(appModule)
    }
}