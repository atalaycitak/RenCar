package com.example.rencar_pair.di

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.repository.AuthRepositoryImpl
import com.example.rencar_pair.domain.repository.AuthRepository
import com.example.rencar_pair.domain.usecase.LoginUseCase
import com.example.rencar_pair.domain.usecase.RegisterUseCase
import com.example.rencar_pair.presentation.ui.screens.auth.LoginViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single { DataStoreManager(androidContext()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }

    viewModelOf(::LoginViewModel)
}
