package com.example.rencar_pair.di

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.repository.AuthRepositoryImpl
import com.example.rencar_pair.data.repository.LicenseRepositoryImpl
import com.example.rencar_pair.data.repository.VehicleRepositoryImpl
import com.example.rencar_pair.domain.repository.AuthRepository
import com.example.rencar_pair.domain.repository.LicenseRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.usecase.GetAvailableVehiclesUseCase
import com.example.rencar_pair.domain.usecase.GetLicenseStatusUseCase
import com.example.rencar_pair.domain.usecase.LoginUseCase
import com.example.rencar_pair.domain.usecase.UploadLicenseUseCase
import com.example.rencar_pair.presentation.ui.screens.home.HomeViewModel
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { DataStoreManager(androidContext()) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<LicenseRepository> { LicenseRepositoryImpl(get()) }
    single<VehicleRepository> { VehicleRepositoryImpl(get()) }

    factory { LoginUseCase(get()) }
    factory { GetLicenseStatusUseCase(get()) }
    factory { UploadLicenseUseCase(get()) }
    factory { GetAvailableVehiclesUseCase(get()) }

    viewModel { LicenseVerificationViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
}
