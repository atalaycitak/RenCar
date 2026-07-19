package com.example.rencar_pair.presentation.ui.screens.referral

import androidx.compose.runtime.Stable
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState
import com.example.rencar_pair.presentation.mvi.NoEffect

@Stable
data class ReferralState(
    val referralCode: String? = null,
    val isLoading: Boolean = false
) : MviState

sealed interface ReferralIntent : MviIntent {
    data object LoadReferralCode : ReferralIntent
}

typealias ReferralEffect = NoEffect
