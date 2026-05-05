package com.juanpablo0612.tucargo

import android.app.Application
import com.juanpablo0612.tucargo.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TuCargoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@TuCargoApplication)
            androidLogger()
        }
    }
}