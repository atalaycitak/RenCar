package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.local.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenExpiredAuthenticator(
    private val dataStoreManager: DataStoreManager,
    private val authInterceptor: AuthInterceptor
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) {
            authInterceptor.clearCachedToken()
            runBlocking {
                dataStoreManager.clear()
            }
            dataStoreManager.notifyTokenExpired()
        }
        return null
    }
}
