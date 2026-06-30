package com.example.rencar_pair

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.rencar_pair.di.appModule
import com.example.rencar_pair.di.networkModule
import com.example.rencar_pair.presentation.navigation.RenCarNavHost
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MainActivity)
            modules(appModule, networkModule)
        }

        enableEdgeToEdge()
        setContent {
            RenCarTheme {
                RenCarNavHost()
            }
        }
    }
}
