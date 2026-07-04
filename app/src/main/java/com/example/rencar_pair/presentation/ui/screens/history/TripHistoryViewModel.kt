package com.example.rencar_pair.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.rental.GetMyRentalsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripHistoryViewModel(
    private val getMyRentalsUseCase: GetMyRentalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TripHistoryState())
    val state = _state.asStateFlow()

    init {
        onIntent(TripHistoryIntent.LoadHistory)
    }

    fun onIntent(intent: TripHistoryIntent) {
        when (intent) {
            TripHistoryIntent.LoadHistory -> loadHistory()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getMyRentalsUseCase()) {
                is NetworkResult.Success -> _state.update {
                    it.copy(rentals = result.data, isLoading = false)
                }
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
