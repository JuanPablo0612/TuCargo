package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.auth.AuthRemoteDataSource
import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.auth.AuthRepositoryImpl
import com.juanpablo0612.tucargo.data.config.ConfigRepository
import com.juanpablo0612.tucargo.data.config.ConfigRepositoryImpl
import com.juanpablo0612.tucargo.data.places.PlacesRepository
import com.juanpablo0612.tucargo.data.places.PlacesRepositoryImpl
import com.juanpablo0612.tucargo.data.places.createPlacesHttpClient
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.data.document.DocumentRepositoryImpl
import com.juanpablo0612.tucargo.data.quote.QuoteRepository
import com.juanpablo0612.tucargo.data.quote.QuoteRepositoryImpl
import com.juanpablo0612.tucargo.data.tracking.TrackingRepository
import com.juanpablo0612.tucargo.data.tracking.TrackingRepositoryImpl
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.trip.TripRepositoryImpl
import com.juanpablo0612.tucargo.data.user.UserRemoteDataSource
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.data.user.UserRepositoryImpl
import com.juanpablo0612.tucargo.domain.trip.TripTracker
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.functions
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }
    single { Firebase.functions }
    single { Firebase.database }

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
    single<TripRepository> { TripRepositoryImpl(get(), get(), get()) }
    single<QuoteRepository> { QuoteRepositoryImpl(get()) }
    single<TrackingRepository> { TrackingRepositoryImpl(get(), get()) }
    single { TripTracker(get(), get(), get(named("ApplicationScope"))) }
    single { createPlacesHttpClient() }
    single<PlacesRepository> { PlacesRepositoryImpl(get(), get()) }
}
