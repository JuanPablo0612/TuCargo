// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/di/Modules.kt

package com.juanpablo0612.tucargo.di

import com.juanpablo0612.tucargo.data.auth.AuthRepository
import com.juanpablo0612.tucargo.data.trip.TripRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.features.auth.presentation.documents.DocumentViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.login.LoginViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.register.RegisterViewModel
import com.juanpablo0612.tucargo.features.client.home.ClientHomeViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    singleOf(::AuthRepository)
    singleOf(::UserRepository)
    singleOf(::TripRepository)
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::DocumentViewModel)
    viewModelOf(::ClientHomeViewModel)
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
