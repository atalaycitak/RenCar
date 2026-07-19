package com.example.rencar_pair.presentation.ui.screens.settings

import androidx.compose.runtime.Stable
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState
import com.example.rencar_pair.presentation.mvi.NoEffect
import com.example.rencar_pair.ui.theme.ThemeMode

@Stable
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) : MviState

sealed interface SettingsIntent : MviIntent {
    data class ChangeThemeMode(val mode: ThemeMode) : SettingsIntent
}

typealias SettingsEffect = NoEffect
