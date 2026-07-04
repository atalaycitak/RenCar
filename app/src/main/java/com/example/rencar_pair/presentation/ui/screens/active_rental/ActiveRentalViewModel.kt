package com.example.rencar_pair.presentation.ui.screens.active_rental

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.rental.FinishRentalUseCase
import com.example.rencar_pair.domain.usecase.rental.GetActiveRentalUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class ActiveRentalViewModel(
    private val getActiveRentalUseCase: GetActiveRentalUseCase,
    private val finishRentalUseCase: FinishRentalUseCase
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
            when (val result = getActiveRentalUseCase(rentalId)) {
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
            when (val result = finishRentalUseCase(rentalId)) {
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
        val current = currentState()
        if (current.rental != null && !current.isFinishing) {
            // Placeholder client-side simulation.
            // Replace with backend-driven cost/distance when rental tracking API is available.
            val addedCost = 2.5
            val addedDistance = 0.5
            updateState {
                it.copy(
                    elapsedMinutes = current.elapsedMinutes + 1,
                    currentCost = current.currentCost + addedCost,
                    distanceKm = current.distanceKm + addedDistance
                )
            }
        }
    }
}
