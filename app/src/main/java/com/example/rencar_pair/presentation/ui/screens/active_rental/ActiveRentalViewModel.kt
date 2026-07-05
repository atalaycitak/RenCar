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
            when (val result = rentalUseCases.getRental(rentalId)) {
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
                delay(TICK_INTERVAL_MS)
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
        emitEffect(ActiveRentalEffect.NavigateToReturnVehicle(rentalId))
    }

    private fun updateSimulation() {
        val current = currentState()
        if (current.rental != null && !current.isFinishing) {
            updateState {
                it.copy(
                    elapsedMinutes = current.elapsedMinutes + 1,
                    currentCost = current.currentCost + COST_PER_MINUTE,
                    distanceKm = current.distanceKm + DISTANCE_PER_MINUTE_KM
                )
            }
        }
    }

    private companion object {
        const val TICK_INTERVAL_MS = 60_000L
        const val COST_PER_MINUTE = 2.5
        const val DISTANCE_PER_MINUTE_KM = 0.5
    }
}
