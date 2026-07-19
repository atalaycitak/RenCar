package com.example.rencar_pair.presentation.ui.components.iyzico

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun IyzicoPaymentWebViewRoute(
    price: Double,
    description: String? = null,
    basketId: String? = null,
    onPaymentSucceeded: (paymentId: String) -> Unit = {},
    onPaymentFailed: (reason: String) -> Unit = {},
    onPaymentCancelled: () -> Unit = {},
    viewModel: IyzicoPaymentWebViewViewModel = koinViewModel { parametersOf(price, description, basketId) }
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is IyzicoPaymentWebViewContract.Effect.ShowPaymentSucceeded -> onPaymentSucceeded(effect.paymentId)
                is IyzicoPaymentWebViewContract.Effect.ShowPaymentFailed    -> onPaymentFailed(effect.reason)
                IyzicoPaymentWebViewContract.Effect.ShowPaymentCancelled   -> onPaymentCancelled()
            }
        }
    }

    Dialog(
        onDismissRequest = { viewModel.onIntent(IyzicoPaymentWebViewContract.Intent.Dismissed) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        IyzicoPaymentWebViewScreen(state = state, onIntent = viewModel::onIntent)
    }
}
