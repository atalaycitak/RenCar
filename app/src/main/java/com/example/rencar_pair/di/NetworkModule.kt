package com.example.rencar_pair.di

import com.example.rencar_pair.BuildConfig
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.AuthInterceptor
import com.example.rencar_pair.data.remote.TokenExpiredAuthenticator
import com.example.rencar_pair.data.remote.TokenHolder
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

private const val BACKEND_HOST = "rencarv2.halitkalayci.com"
private const val BACKEND_BASE_URL = "https://$BACKEND_HOST/"
private val BACKEND_FALLBACK_IPS = listOf("104.21.46.25", "172.67.222.231")

val networkModule = module {

    single { TokenHolder() }

    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    single {
        AuthInterceptor(get())
    }

    single {
        TokenExpiredAuthenticator(get(), get())
    }

    single<Dns> {
        object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return try {
                    Dns.SYSTEM.lookup(hostname)
                } catch (error: UnknownHostException) {
                    if (hostname.equals(BACKEND_HOST, ignoreCase = true)) {
                        BACKEND_FALLBACK_IPS.map { InetAddress.getByName(it) }
                    } else {
                        throw error
                    }
                }
            }
        }
    }

    single {
        OkHttpClient.Builder()
            .dns(get())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenExpiredAuthenticator>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    single {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory(contentType))
            .build()
    }

    single {
        get<Retrofit>().create(RenCarApi::class.java)
    }
}
