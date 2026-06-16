package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.features.admin.home.AdminHomeViewModel
import com.juanpablo0612.tucargo.features.admin.review.AdminDriverReviewViewModel
import com.juanpablo0612.tucargo.features.auth.AuthViewModel
import com.juanpablo0612.tucargo.features.auth.documents.KycPendingViewModel
import com.juanpablo0612.tucargo.features.auth.driverdocs.DriverDocsUploadViewModel
import com.juanpablo0612.tucargo.features.auth.login.LoginViewModel
import com.juanpablo0612.tucargo.features.auth.register.RegisterViewModel
import com.juanpablo0612.tucargo.features.auth.resetpassword.ResetPasswordViewModel
import com.juanpablo0612.tucargo.features.auth.vehicle.VehicleRegistrationViewModel
import com.juanpablo0612.tucargo.features.client.createtrip.CreateTripViewModel
import com.juanpablo0612.tucargo.features.client.home.ClientHomeViewModel
import com.juanpablo0612.tucargo.features.client.quote.TripRequestViewModel
import com.juanpablo0612.tucargo.features.driver.home.DriverHomeViewModel
import com.juanpablo0612.tucargo.features.trip.active.TripActiveViewModel
import com.juanpablo0612.tucargo.features.trip.completed.TripCompletedViewModel
import com.juanpablo0612.tucargo.features.trip.detail.TripDetailViewModel
import com.juanpablo0612.tucargo.features.trip.history.TripHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ClientHomeViewModel)
    viewModelOf(::CreateTripViewModel)
    viewModelOf(::TripRequestViewModel)
    viewModelOf(::DriverHomeViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::VehicleRegistrationViewModel)
    viewModelOf(::DriverDocsUploadViewModel)
    viewModelOf(::KycPendingViewModel)
    viewModelOf(::TripHistoryViewModel)
    viewModel { (tripId: String) -> TripDetailViewModel(tripId, get(), get(), get(), get()) }
    viewModel { (tripId: String) -> TripActiveViewModel(tripId, get(), get(), get(), get()) }
    viewModel { (tripId: String) -> TripCompletedViewModel(tripId, get(), get()) }
    viewModelOf(::AdminHomeViewModel)
    viewModel { (driverId: String) -> AdminDriverReviewViewModel(driverId, get(), get(), get()) }
}
