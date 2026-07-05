package com.example.rencar_pair.presentation.ui.screens.active_rental

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class ActiveRentalViewModel(
    private val rentalUseCases: RentalUseCases
) : BaseMviViewModel<ActiveRentalState, ActiveRentalIntent, ActiveRentalEffect>(
    ActiveRentalState()
) {

    private var timerJob: Job? = null

    override fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> loadRental(intent.rentalId)
            ActiveRentalIntent.FinishRental -> finishRental()
            ActiveRentalIntent.TickTime -> updateSimulation()
        }
    }

    private fun loadRental(rentalId: String) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = rentalUseCases.getActiveRental(rentalId)) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            rental = result.data,
                            elapsedMinutes = 0,
                            distanceKm = 0.0,
                            currentCost = result.data.totalPrice
                        )
                    }
                    startTimer()
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = launchCoroutine {
            while (isActive) {
                delay(60000)
                onIntent(ActiveRentalIntent.TickTime)
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private fun finishRental() {
        val rentalId = currentState().rental?.id ?: return
        launchCoroutine {
            updateState { it.copy(isFinishing = true, errorMessage = null) }
            when (val result = rentalUseCases.finishRental(rentalId)) {
                is NetworkResult.Success -> {
                    timerJob?.cancel()
                    updateState { it.copy(isFinishing = false, rental = result.data) }
                    emitEffect(ActiveRentalEffect.NavigateToSummary(rentalId))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isFinishing = false, errorMessage = result.message) }
                    emitEffect(ActiveRentalEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun updateSimulation() {
        updateState { current ->
            if (current.rental != null && !current.isFinishing) {
                current.copy(
                    elapsedMinutes = current.elapsedMinutes + 1,
                    currentCost = current.currentCost + 2.5,
                    distanceKm = current.distanceKm + 0.5
                )
            } else {
                current
            }
        }
    }
}
