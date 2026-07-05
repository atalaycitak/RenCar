package com.example.rencar_pair

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.presentation.navigation.LoginRoute
import com.example.rencar_pair.presentation.navigation.RenCarNavHost
import com.example.rencar_pair.presentation.navigation.SplashRoute
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            RenCarTheme {
                val navController = rememberNavController()
                val dataStoreManager: DataStoreManager = koinInject()

                LaunchedEffect(dataStoreManager) {
                    dataStoreManager.tokenExpired.collect {
                        navController.navigate(LoginRoute(sessionExpired = true)) {
                            popUpTo<SplashRoute> { inclusive = true }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RenCarNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
