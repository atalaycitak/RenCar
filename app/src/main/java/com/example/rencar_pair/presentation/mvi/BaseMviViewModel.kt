package com.example.rencar_pair.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseMviViewModel<STATE : MviState, INTENT : MviIntent, EFFECT : MviEffect>(
    initialState: STATE
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _effect = Channel<EFFECT>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    protected fun currentState(): STATE = _state.value

    protected fun updateState(transform: (STATE) -> STATE) {
        _state.update(transform)
    }

    protected fun emitEffect(effect: EFFECT) {
        _effect.trySend(effect)
    }

    protected fun launchCoroutine(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch {
            block()
        }

    abstract fun onIntent(intent: INTENT)
}
