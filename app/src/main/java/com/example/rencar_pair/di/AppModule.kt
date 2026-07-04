package com.example.rencar_pair.di

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.location.DefaultLocationTracker
import com.example.rencar_pair.data.repository.AuthRepositoryImpl
import com.example.rencar_pair.data.repository.LicenseRepositoryImpl
import com.example.rencar_pair.data.repository.ReservationRepositoryImpl
import com.example.rencar_pair.data.repository.VehicleRepositoryImpl
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.repository.AuthRepository
import com.example.rencar_pair.domain.repository.LicenseRepository
import com.example.rencar_pair.domain.repository.PaymentRepository
import com.example.rencar_pair.domain.repository.ReservationRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.repository.WalletRepository
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.CreateRentalUseCase
import com.example.rencar_pair.domain.usecase.GetAvailableVehiclesUseCase
import com.example.rencar_pair.domain.usecase.GetCurrentUserUseCase
import com.example.rencar_pair.domain.usecase.GetLicenseStatusUseCase
import com.example.rencar_pair.domain.usecase.GetVehicleDetailUseCase
import com.example.rencar_pair.domain.usecase.LoginUseCase
import com.example.rencar_pair.domain.usecase.LogoutUseCase
import com.example.rencar_pair.domain.usecase.RefreshSessionUseCase
import com.example.rencar_pair.domain.usecase.RegisterUseCase
import com.example.rencar_pair.domain.usecase.UploadLicenseUseCase
import com.example.rencar_pair.domain.usecase.VerifyOtpUseCase
import com.example.rencar_pair.domain.usecase.rental.FinishRentalUseCase
import com.example.rencar_pair.domain.usecase.rental.GetActiveRentalUseCase
import com.example.rencar_pair.domain.usecase.payment.GetWalletBalanceUseCase
import com.example.rencar_pair.domain.usecase.payment.GetWalletInfoUseCase
import com.example.rencar_pair.domain.usecase.payment.GetSavedCardsUseCase
import com.example.rencar_pair.domain.usecase.payment.ProcessPaymentUseCase
import com.example.rencar_pair.domain.usecase.payment.TopUpWalletUseCase
import com.example.rencar_pair.presentation.ui.screens.active_rental.ActiveRentalViewModel
import com.example.rencar_pair.presentation.ui.screens.auth.LoginViewModel
import com.example.rencar_pair.presentation.ui.screens.auth.RegisterViewModel
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpViewModel
import com.example.rencar_pair.presentation.ui.screens.delivery.DeliveryChecklistViewModel
import com.example.rencar_pair.presentation.ui.screens.home.HomeViewModel
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationViewModel
import com.example.rencar_pair.presentation.ui.screens.reservation.ReservationViewModel
import com.example.rencar_pair.presentation.ui.screens.trip_summary.TripSummaryViewModel
import com.example.rencar_pair.presentation.ui.screens.vehicle.VehicleDetailViewModel
import com.example.rencar_pair.presentation.ui.screens.wallet.WalletViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single { DataStoreManager(androidContext()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<LicenseRepository> { LicenseRepositoryImpl(get(), androidContext()) }
    single<VehicleRepository> { VehicleRepositoryImpl(get()) }
    single<ReservationRepository> { ReservationRepositoryImpl(get()) }

    single<PaymentRepository> { com.example.rencar_pair.data.repository.PaymentRepositoryImpl() }
    single<WalletRepository> { com.example.rencar_pair.data.repository.WalletRepositoryImpl() }

    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    single<LocationTracker> { DefaultLocationTracker(get(), androidContext()) }

    factory { LoginUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { RefreshSessionUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetLicenseStatusUseCase(get()) }
    factory { UploadLicenseUseCase(get()) }
    factory { GetAvailableVehiclesUseCase(get()) }
    factory { GetVehicleDetailUseCase(get()) }
    factory { CalculateReservationQuoteUseCase() }
    factory { CreateRentalUseCase(get()) }

    factory { GetActiveRentalUseCase(get()) }
    factory { FinishRentalUseCase(get()) }

    factory { GetWalletInfoUseCase(get()) }
    factory { GetWalletBalanceUseCase(get()) }
    factory { GetSavedCardsUseCase(get()) }
    factory { TopUpWalletUseCase(get()) }
    factory { ProcessPaymentUseCase(get()) }

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
}
