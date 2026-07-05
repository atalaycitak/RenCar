package com.example.rencar_pair.presentation.ui.screens.history

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import com.example.rencar_pair.presentation.mvi.NoEffect

class TripHistoryViewModel(
    private val rentalUseCases: RentalUseCases
) : BaseMviViewModel<TripHistoryState, TripHistoryIntent, NoEffect>(TripHistoryState()) {

    init {
        onIntent(TripHistoryIntent.LoadHistory)
    }

    override fun onIntent(intent: TripHistoryIntent) {
        when (intent) {
            TripHistoryIntent.LoadHistory -> loadHistory()
        }
    }

    private fun loadHistory() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = rentalUseCases.getMyRentals()) {
                is NetworkResult.Success -> updateState {
                    it.copy(rentals = result.data, isLoading = false)
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
