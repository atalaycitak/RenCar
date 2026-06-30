package com.example.rencar_pair.di

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.repository.AuthRepositoryImpl
import com.example.rencar_pair.domain.repository.AuthRepository
import com.example.rencar_pair.domain.usecase.LoginUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single { DataStoreManager(androidContext()) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }

    factory { LoginUseCase(get()) }
}
