package com.example.rencar_pair

import android.app.Application
import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.remote.TokenHolder
import com.example.rencar_pair.di.appModule
import com.example.rencar_pair.di.networkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RenCarApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RenCarApplication)
            modules(appModule, networkModule)
        }
        eagerlyLoadToken()
    }

    private fun eagerlyLoadToken() {
        applicationScope.launch {
            val dataStoreManager: DataStoreManager = GlobalContext.get().get()
            val tokenHolder: TokenHolder = GlobalContext.get().get()
            val token = dataStoreManager.authToken.firstOrNull()
            tokenHolder.token = token
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}
