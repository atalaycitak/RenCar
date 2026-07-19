package com.example.rencar_pair.presentation.ui.screens.referral

import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.delay

class ReferralViewModel : BaseMviViewModel<ReferralState, ReferralIntent, ReferralEffect>(
    ReferralState()
) {
    init {
        onIntent(ReferralIntent.LoadReferralCode)
    }

    override fun onIntent(intent: ReferralIntent) {
        when (intent) {
            is ReferralIntent.LoadReferralCode -> loadReferralCodeMock()
        }
    }

    private fun loadReferralCodeMock() {
        launchCoroutine {
            updateState { it.copy(isLoading = true) }
            delay(800) // Mock network delay
            updateState { it.copy(isLoading = false, referralCode = "REB-QW19K2") }
        }
    }
}
