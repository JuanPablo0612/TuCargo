package com.juanpablo0612.tucargo.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    includes(dataModule, domainModule, viewModelModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(appModule, platformModule)
    }
}
