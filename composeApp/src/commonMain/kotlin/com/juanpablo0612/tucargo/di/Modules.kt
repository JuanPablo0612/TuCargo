package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.core.location.MockLocationProvider
import com.juanpablo0612.tucargo.data.auth.AuthRemoteDataSource
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.auth.AuthRepositoryImpl
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.data.document.DocumentRepositoryImpl
import com.juanpablo0612.tucargo.data.config.SystemConfig
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.trip.TripRepositoryImpl
import com.juanpablo0612.tucargo.data.trip.TripTrackingManager
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.data.user.UserRepositoryImpl
import com.juanpablo0612.tucargo.domain.usecase.CreateTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.UploadDocumentsUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetClientTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.IsUserLoggedInUseCase
import com.juanpablo0612.tucargo.domain.usecase.LoginUseCase
import com.juanpablo0612.tucargo.domain.usecase.LogoutUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveAuthStateUseCase
import com.juanpablo0612.tucargo.domain.usecase.RegisterUseCase
import com.juanpablo0612.tucargo.domain.usecase.SendPasswordResetEmailUseCase
import com.juanpablo0612.tucargo.domain.usecase.SignOutUseCase
import com.juanpablo0612.tucargo.domain.usecase.UpdateDriverStatusUseCase
import com.juanpablo0612.tucargo.features.auth.presentation.AuthViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.documents.DocumentViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.login.LoginViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.register.RegisterViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.resetpassword.ResetPasswordViewModel
import com.juanpablo0612.tucargo.features.client.home.ClientHomeViewModel
import com.juanpablo0612.tucargo.features.driver.home.presentation.DriverHomeViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
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
    single { Firebase.storage }

    single { AuthRemoteDataSource(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    single<DocumentRepository> { DocumentRepositoryImpl(get(), get()) }
    single<LocationProvider> { MockLocationProvider() }
    single { SystemConfig() }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<TripRepository> { TripRepositoryImpl(get()) }
    single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    single { TripTrackingManager(get(), get(), get()) }
}

val domainModule = module {
    singleOf(::LoginUseCase)
    singleOf(::RegisterUseCase)
    singleOf(::GetCurrentUserUseCase)
    singleOf(::GetCurrentUserIdUseCase)
    singleOf(::IsUserLoggedInUseCase)
    singleOf(::SignOutUseCase)
    singleOf(::UpdateDriverStatusUseCase)
    singleOf(::GetClientTripsUseCase)
    singleOf(::CreateTripUseCase)
    singleOf(::UploadDocumentsUseCase)

    singleOf(::LogoutUseCase)
    singleOf(::SendPasswordResetEmailUseCase)
    singleOf(::ObserveAuthStateUseCase)
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::DocumentViewModel)
    viewModelOf(::ClientHomeViewModel)
    viewModelOf(::DriverHomeViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::AuthViewModel)
}

val appModule = module {
    includes(dataModule, domainModule, viewModelModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(appModule)
    }
}
