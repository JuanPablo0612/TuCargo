package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.domain.usecase.admin.GetPendingDriversUseCase
import com.juanpablo0612.tucargo.domain.usecase.admin.ReviewKycDocumentUseCase
import com.juanpablo0612.tucargo.domain.usecase.admin.SetDriverVerifiedUseCase
import com.juanpablo0612.tucargo.domain.usecase.auth.LoginUseCase
import com.juanpablo0612.tucargo.domain.usecase.auth.LogoutUseCase
import com.juanpablo0612.tucargo.domain.usecase.auth.ObserveAuthStateUseCase
import com.juanpablo0612.tucargo.domain.usecase.auth.RegisterUseCase
import com.juanpablo0612.tucargo.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.juanpablo0612.tucargo.domain.usecase.document.ObserveKycDocumentsUseCase
import com.juanpablo0612.tucargo.domain.usecase.document.UploadKycDocumentUseCase
import com.juanpablo0612.tucargo.domain.usecase.quote.ComputeQuoteUseCase
import com.juanpablo0612.tucargo.domain.usecase.quote.RequestQuoteUseCase
import com.juanpablo0612.tucargo.domain.usecase.tracking.FlushLocationBufferUseCase
import com.juanpablo0612.tucargo.domain.usecase.tracking.ObserveDriverLocationUseCase
import com.juanpablo0612.tucargo.domain.usecase.tracking.SendLocationUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.AcceptOfferUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.AcceptTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.AdvanceTripStatusUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.CalculateTripPriceUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.CancelTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.CompleteTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.CreateTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.GetClientTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.GetDriverTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.ObserveActiveOfferUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.ObserveAvailableTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.ObserveDriverActiveTripsUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.ObserveTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.RejectOfferUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.RequestTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.GetCurrentUserIdUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.GetDriverOnboardingStatusUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.IsUserLoggedInUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.ObserveCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.RegisterVehicleUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.ToggleAvailabilityUseCase
import com.juanpablo0612.tucargo.domain.usecase.user.UpdateDriverStatusUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::LoginUseCase)
    singleOf(::RegisterUseCase)
    singleOf(::GetCurrentUserUseCase)
    singleOf(::GetCurrentUserIdUseCase)
    singleOf(::IsUserLoggedInUseCase)
    singleOf(::UpdateDriverStatusUseCase)
    singleOf(::ToggleAvailabilityUseCase)
    singleOf(::ObserveDriverActiveTripsUseCase)
    singleOf(::GetClientTripsUseCase)
    singleOf(::GetDriverTripsUseCase)
    singleOf(::CreateTripUseCase)
    singleOf(::CalculateTripPriceUseCase)
    singleOf(::ComputeQuoteUseCase)
    singleOf(::RequestQuoteUseCase)
    singleOf(::RequestTripUseCase)
    singleOf(::ObserveTripUseCase)
    singleOf(::ObserveAvailableTripsUseCase)
    singleOf(::AcceptTripUseCase)
    singleOf(::AcceptOfferUseCase)
    singleOf(::RejectOfferUseCase)
    singleOf(::ObserveActiveOfferUseCase)
    singleOf(::AdvanceTripStatusUseCase)
    singleOf(::CompleteTripUseCase)
    singleOf(::CancelTripUseCase)
    singleOf(::SendLocationUseCase)
    singleOf(::FlushLocationBufferUseCase)
    singleOf(::ObserveDriverLocationUseCase)

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
