package com.example.rencar_pair.presentation.ui.components.iyzico

object IyzicoPaymentWebViewContract {

    const val CHECKOUT_FORM_CALLBACK_PATH = "iyzico/checkout-form/callback"

    data class State(
        val isLoading: Boolean = true,
        val paymentPageUrl: String? = null,
        val errorMessage: String? = null
    )

    sealed interface Intent {
        data class CallbackUrlReached(val url: String) : Intent
        data object Dismissed : Intent
    }

    sealed interface Effect {
        data class ShowPaymentSucceeded(val paymentId: String) : Effect
        data class ShowPaymentFailed(val reason: String) : Effect
        data object ShowPaymentCancelled : Effect
    }
}
