package com.example.rencar_pair

import android.app.Application
import com.example.rencar_pair.di.appModule
import com.example.rencar_pair.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RenCarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RenCarApplication)
            modules(appModule, networkModule)
        }
    }
}
