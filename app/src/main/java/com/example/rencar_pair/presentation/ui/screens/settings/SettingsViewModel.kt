package com.example.rencar_pair.presentation.ui.screens.settings

import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import com.example.rencar_pair.ui.theme.ThemeManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsViewModel : BaseMviViewModel<SettingsState, SettingsIntent, SettingsEffect>(
    SettingsState(themeMode = ThemeManager.themeMode.value)
) {
    init {
        ThemeManager.themeMode.onEach { mode ->
            updateState { it.copy(themeMode = mode) }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ChangeThemeMode -> {
                ThemeManager.setTheme(intent.mode)
            }
        }
    }
}
