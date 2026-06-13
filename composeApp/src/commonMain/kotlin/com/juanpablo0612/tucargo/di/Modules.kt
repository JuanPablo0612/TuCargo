package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.core.location.LocationProvider
import com.juanpablo0612.tucargo.data.auth.AuthRemoteDataSource
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.auth.AuthRepositoryImpl
import com.juanpablo0612.tucargo.data.user.UserRemoteDataSource
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.data.document.DocumentRepositoryImpl
import com.juanpablo0612.tucargo.data.config.ConfigRepository
import com.juanpablo0612.tucargo.data.config.ConfigRepositoryImpl
import com.juanpablo0612.tucargo.data.config.SystemConfig
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.trip.TripRepositoryImpl
import com.juanpablo0612.tucargo.domain.trip.TripTracker
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.data.user.UserRepositoryImpl
import com.juanpablo0612.tucargo.domain.usecase.AcceptTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.AdvanceTripStatusUseCase
import com.juanpablo0612.tucargo.domain.usecase.CalculateTripPriceUseCase
import com.juanpablo0612.tucargo.domain.usecase.CancelTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.CreateTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetClientTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetDriverTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetDriverOnboardingStatusUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetPendingDriversUseCase
import com.juanpablo0612.tucargo.domain.usecase.IsUserLoggedInUseCase
import com.juanpablo0612.tucargo.domain.usecase.LoginUseCase
import com.juanpablo0612.tucargo.domain.usecase.LogoutUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveAuthStateUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveAvailableTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveDriverActiveTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveKycDocumentsUseCase
import com.juanpablo0612.tucargo.domain.usecase.ObserveTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.RegisterUseCase
import com.juanpablo0612.tucargo.domain.usecase.RegisterVehicleUseCase
import com.juanpablo0612.tucargo.domain.usecase.ReviewKycDocumentUseCase
import com.juanpablo0612.tucargo.domain.usecase.SendPasswordResetEmailUseCase
import com.juanpablo0612.tucargo.domain.usecase.SetDriverVerifiedUseCase
import com.juanpablo0612.tucargo.domain.usecase.UpdateDriverStatusUseCase
import com.juanpablo0612.tucargo.domain.usecase.UploadKycDocumentUseCase
import com.juanpablo0612.tucargo.features.admin.home.AdminHomeViewModel
import com.juanpablo0612.tucargo.features.admin.review.AdminDriverReviewViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.AuthViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.documents.KycPendingViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.driverdocs.DriverDocsUploadViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.login.LoginViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.register.RegisterViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.resetpassword.ResetPasswordViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.vehicle.VehicleRegistrationViewModel
import com.juanpablo0612.tucargo.features.client.createtrip.CreateTripViewModel
import com.juanpablo0612.tucargo.features.client.home.ClientHomeViewModel
import com.juanpablo0612.tucargo.features.driver.home.presentation.DriverHomeViewModel
import com.juanpablo0612.tucargo.features.trip.presentation.active.TripActiveViewModel
import com.juanpablo0612.tucargo.features.trip.presentation.detail.TripDetailViewModel
import com.juanpablo0612.tucargo.features.trip.presentation.history.TripHistoryViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }

    single { AppDispatchers() }
    single(named("ApplicationScope")) {
        CoroutineScope(SupervisorJob() + get<AppDispatchers>().default)
    }

    singleOf(::AuthRemoteDataSource)
    singleOf(::UserRemoteDataSource)
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }

    single<DocumentRepository> { DocumentRepositoryImpl(get(), get(), get()) }
    single<ConfigRepository> { ConfigRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
    single<TripRepository> { TripRepositoryImpl(get(), get()) }
    single { TripTracker(get(), get(), get(named("ApplicationScope"))) }
}

val domainModule = module {
    singleOf(::LoginUseCase)
    singleOf(::RegisterUseCase)
    singleOf(::GetCurrentUserUseCase)
    singleOf(::GetCurrentUserIdUseCase)
    singleOf(::IsUserLoggedInUseCase)
    singleOf(::UpdateDriverStatusUseCase)
    singleOf(::ObserveDriverActiveTripsUseCase)
    singleOf(::GetClientTripsUseCase)
    singleOf(::GetDriverTripsUseCase)
    singleOf(::CreateTripUseCase)
    singleOf(::CalculateTripPriceUseCase)
    singleOf(::ObserveTripUseCase)
    singleOf(::ObserveAvailableTripsUseCase)
    singleOf(::AcceptTripUseCase)
    singleOf(::AdvanceTripStatusUseCase)
    singleOf(::CancelTripUseCase)

    singleOf(::LogoutUseCase)
    singleOf(::SendPasswordResetEmailUseCase)
    singleOf(::ObserveAuthStateUseCase)
    singleOf(::RegisterVehicleUseCase)
    singleOf(::GetDriverOnboardingStatusUseCase)
    singleOf(::ObserveCurrentUserUseCase)
    singleOf(::UploadKycDocumentUseCase)
    singleOf(::ObserveKycDocumentsUseCase)

    singleOf(::GetPendingDriversUseCase)
    singleOf(::ReviewKycDocumentUseCase)
    singleOf(::SetDriverVerifiedUseCase)
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ClientHomeViewModel)
    viewModelOf(::CreateTripViewModel)
    viewModelOf(::DriverHomeViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::VehicleRegistrationViewModel)
    viewModelOf(::DriverDocsUploadViewModel)
    viewModelOf(::KycPendingViewModel)
    viewModelOf(::TripHistoryViewModel)
    viewModel { (tripId: String) -> TripDetailViewModel(tripId, get(), get(), get()) }
    viewModel { (tripId: String) -> TripActiveViewModel(tripId, get(), get()) }
    viewModelOf(::AdminHomeViewModel)
    viewModel { (driverId: String) -> AdminDriverReviewViewModel(driverId, get(), get(), get()) }
}

val appModule = module {
    includes(dataModule, domainModule, viewModelModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(appModule, platformModule)
    }
}
