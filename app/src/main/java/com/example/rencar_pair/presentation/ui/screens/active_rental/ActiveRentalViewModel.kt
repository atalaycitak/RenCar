package com.example.rencar_pair.presentation.ui.screens.active_rental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.rental.FinishRentalUseCase
import com.example.rencar_pair.domain.usecase.rental.GetActiveRentalUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ActiveRentalViewModel(
    private val getActiveRentalUseCase: GetActiveRentalUseCase,
    private val finishRentalUseCase: FinishRentalUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ActiveRentalState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ActiveRentalEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(60000) // tick every minute
                onIntent(ActiveRentalIntent.TickTime)
            }
        }
    }

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> loadRental(intent.rentalId)
            ActiveRentalIntent.FinishRental -> finishRental()
            ActiveRentalIntent.TickTime -> updateSimulation()
        }
    }

    private fun loadRental(rentalId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getActiveRentalUseCase(rentalId)) {
                is NetworkResult.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            rental = result.data,
                            elapsedMinutes = 0,
                            distanceKm = 0.0,
                            currentCost = result.data.totalPrice
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun finishRental() {
        val rentalId = state.value.rental?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isFinishing = true, errorMessage = null) }
            when (val result = finishRentalUseCase(rentalId)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isFinishing = false, rental = result.data) }
                    _effect.send(ActiveRentalEffect.NavigateToSummary(rentalId))
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isFinishing = false, errorMessage = result.message) }
                    _effect.send(ActiveRentalEffect.ShowError(result.message ?: "Failed to finish"))
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun updateSimulation() {
        val current = state.value
        if (current.rental != null && !current.isFinishing) {
            val newMins = current.elapsedMinutes + 1
            val addedCost = 2.5 // simulate 2.5 tl per minute
            val newCost = current.currentCost + addedCost
            val newDistance = current.distanceKm + 0.5 // simulate 0.5 km per minute
            _state.update { 
                it.copy(
                    elapsedMinutes = newMins,
                    currentCost = newCost,
                    distanceKm = newDistance
                )
            }
        }
    }
}
