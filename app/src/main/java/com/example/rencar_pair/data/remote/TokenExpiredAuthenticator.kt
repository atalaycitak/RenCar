package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.local.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenExpiredAuthenticator(
    private val tokenHolder: TokenHolder,
    private val dataStoreManager: DataStoreManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) {
            tokenHolder.token = null
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                dataStoreManager.clear()
                dataStoreManager.notifyTokenExpired()
            }
        }
        return null
    }
}
