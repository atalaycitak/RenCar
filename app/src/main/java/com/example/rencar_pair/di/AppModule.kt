package com.example.rencar_pair.di

import com.example.rencar_pair.RenCarApplication
import com.example.rencar_pair.BuildConfig
import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.location.DefaultLocationTracker
import com.example.rencar_pair.data.repository.*
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.repository.AuthRepository
import com.example.rencar_pair.domain.repository.LicenseRepository
import com.example.rencar_pair.domain.repository.PaymentRepository
import com.example.rencar_pair.domain.repository.RentalRepository
import com.example.rencar_pair.domain.repository.ReservationRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import com.example.rencar_pair.domain.repository.WalletRepository
import com.example.rencar_pair.domain.usecase.AuthUseCases
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.LicenseUseCases
import com.example.rencar_pair.domain.usecase.LoginUseCase
import com.example.rencar_pair.domain.usecase.PaymentUseCases
import com.example.rencar_pair.domain.usecase.RegisterUseCase
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.domain.usecase.VerifyOtpUseCase
import com.example.rencar_pair.domain.usecase.rental.ReturnVehicleUseCase
import com.example.rencar_pair.presentation.ui.screens.active_rental.ActiveRentalViewModel
import com.example.rencar_pair.presentation.ui.screens.auth.LoginViewModel
import com.example.rencar_pair.presentation.ui.screens.auth.RegisterViewModel
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpViewModel
import com.example.rencar_pair.presentation.ui.screens.delivery.DeliveryChecklistViewModel
import com.example.rencar_pair.presentation.ui.screens.home.HomeViewModel
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationViewModel
import com.example.rencar_pair.presentation.ui.screens.reservation.ReservationViewModel
import com.example.rencar_pair.presentation.ui.screens.splash.SplashViewModel
import com.example.rencar_pair.presentation.ui.screens.trip_summary.TripSummaryViewModel
import com.example.rencar_pair.presentation.ui.screens.vehicle.VehicleDetailViewModel
import com.example.rencar_pair.presentation.ui.screens.wallet.WalletViewModel
import com.example.rencar_pair.presentation.ui.screens.profile.ProfileViewModel
import com.example.rencar_pair.presentation.ui.screens.history.TripHistoryViewModel
import com.example.rencar_pair.presentation.ui.screens.return_vehicle.ReturnVehicleViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single { DataStoreManager(androidContext()) }

    single { (androidContext() as RenCarApplication).applicationScope }

    // Repositories
    single<AuthRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakeAuthRepository() else DefaultAuthRepository(get(), get(), get(), get())
    }
    single<LicenseRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakeLicenseRepository() else DefaultLicenseRepository(get(), androidContext())
    }
    single<VehicleRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakeVehicleRepository() else DefaultVehicleRepository(get())
    }
    single<VehicleLocationRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakeVehicleLocationRepository() else DefaultVehicleLocationRepository(get())
    }
    single<ReservationRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakeReservationRepository() else DefaultReservationRepository(get())
    }
    single<RentalRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakeRentalRepository() else DefaultRentalRepository(get(), androidContext())
    }
    single<PaymentRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakePaymentRepository() else DefaultPaymentRepository(get())
    }
    single<WalletRepository> {
        if (BuildConfig.USE_FAKE_REPOSITORIES) FakeWalletRepository() else DefaultWalletRepository(get())
    }

    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    single<LocationTracker> { DefaultLocationTracker(get(), androidContext()) }

    // Grouped Use Cases (replaces anemic delegation use cases)
    single { AuthUseCases(get()) }
    single { VehicleUseCases(get()) }
    single { LicenseUseCases(get()) }
    single { RentalUseCases(get(), get()) }
    single { PaymentUseCases(get(), get()) }

    // Use cases with real business logic (validation, calculations)
    single { LoginUseCase(get()) }
    single { VerifyOtpUseCase(get()) }
    single { RegisterUseCase(get()) }
    single { CalculateReservationQuoteUseCase() }
    single { ReturnVehicleUseCase(get()) }
    single { com.example.rencar_pair.domain.usecase.rental.RentalPhotoUseCases(get()) }

    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::VerifyOtpViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::LicenseVerificationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::VehicleDetailViewModel)
    viewModelOf(::ReservationViewModel)
    viewModelOf(::DeliveryChecklistViewModel)
    viewModelOf(::ActiveRentalViewModel)
    viewModelOf(::TripSummaryViewModel)
    viewModelOf(::WalletViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::TripHistoryViewModel)
    viewModelOf(::ReturnVehicleViewModel)
}
