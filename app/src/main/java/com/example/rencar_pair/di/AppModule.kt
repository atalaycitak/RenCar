package com.example.rencar_pair.di

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.repository.AuthRepositoryImpl
import com.example.rencar_pair.data.repository.LicenseRepositoryImpl
import com.example.rencar_pair.data.repository.ReservationRepositoryImpl
import com.example.rencar_pair.data.repository.VehicleRepositoryImpl
import com.example.rencar_pair.domain.repository.AuthRepository
import com.example.rencar_pair.domain.repository.LicenseRepository
import com.example.rencar_pair.domain.repository.ReservationRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.CreateRentalUseCase
import com.example.rencar_pair.domain.usecase.GetAvailableVehiclesUseCase
import com.example.rencar_pair.domain.usecase.GetLicenseStatusUseCase
import com.example.rencar_pair.domain.usecase.GetVehicleDetailUseCase
import com.example.rencar_pair.domain.usecase.LoginUseCase
import com.example.rencar_pair.domain.usecase.RegisterUseCase
import com.example.rencar_pair.domain.usecase.UploadLicenseUseCase
import com.example.rencar_pair.presentation.ui.screens.delivery.DeliveryChecklistViewModel
import com.example.rencar_pair.presentation.ui.screens.auth.LoginViewModel
import com.example.rencar_pair.presentation.ui.screens.home.HomeViewModel
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationViewModel
import com.example.rencar_pair.presentation.ui.screens.reservation.ReservationViewModel
import com.example.rencar_pair.presentation.ui.screens.vehicle.VehicleDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single { DataStoreManager(androidContext()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<LicenseRepository> { LicenseRepositoryImpl(get()) }
    single<VehicleRepository> { VehicleRepositoryImpl(get()) }
    single<ReservationRepository> { ReservationRepositoryImpl(get()) }

    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { GetLicenseStatusUseCase(get()) }
    factory { UploadLicenseUseCase(get()) }
    factory { GetAvailableVehiclesUseCase(get()) }
    factory { GetVehicleDetailUseCase(get()) }
    factory { CalculateReservationQuoteUseCase() }
    factory { CreateRentalUseCase(get()) }

    viewModelOf(::LoginViewModel)
    viewModelOf(::LicenseVerificationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::VehicleDetailViewModel)
    viewModelOf(::ReservationViewModel)
    viewModelOf(::DeliveryChecklistViewModel)
}
