package com.example.rencar_pair.presentation.ui.components.iyzico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.PaymentRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IyzicoPaymentWebViewViewModel(
    private val price: Double,
    private val description: String?,
    private val basketId: String?,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private var checkoutToken: String? = null
    private var hasResolved = false

    private val _state = MutableStateFlow(IyzicoPaymentWebViewContract.State())
    val state: StateFlow<IyzicoPaymentWebViewContract.State> = _state.asStateFlow()

    private val _effect = Channel<IyzicoPaymentWebViewContract.Effect>(Channel.BUFFERED)
    val effect: Flow<IyzicoPaymentWebViewContract.Effect> = _effect.receiveAsFlow()

    init {
        initializeCheckoutForm()
    }

    fun onIntent(intent: IyzicoPaymentWebViewContract.Intent) {
        when (intent) {
            is IyzicoPaymentWebViewContract.Intent.CallbackUrlReached -> handleCallbackUrlReached()
            IyzicoPaymentWebViewContract.Intent.Dismissed             -> handleDismissed()
        }
    }

    private fun initializeCheckoutForm() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = paymentRepository.initializeCheckoutForm(price, description, basketId)) {
                is NetworkResult.Success -> {
                    val data = result.data
                    if (data.checkoutFormContent.isNullOrBlank() && data.token.isNullOrBlank()) {
                        _state.update { it.copy(isLoading = false, errorMessage = "Ödeme sayfası alınamadı.") }
                        resolve(IyzicoPaymentWebViewContract.Effect.ShowPaymentFailed("Ödeme sayfası alınamadı."))
                    } else {
                        checkoutToken = data.token
                        // Ozan'ın kodu paymentPageUrl diyordu ancak bizim DTO'da token ve checkoutFormContent var.
                        // Iyzico CheckoutForm initialize dönüşü genellikle HTML form içeriği döndürür veya doğrudan bir URL döndürür (paymentPageUrl).
                        // API DTO'muzda paymentPageUrl yok, CheckoutFormInitializeResponse'da checkoutFormContent var. 
                        // WebView'a loadDataWithBaseURL ile HTML yüklemeliyiz veya token içeren ödeme linkine yönlendirmeliyiz.
                        // Ozan'ın IyzicoPaymentResponseDto'sunda paymentPageUrl varmış. 
                        // Iyzico dokümanına göre paymentPageUrl döner. DTO'yu kontrol edelim. Bizim DTO'da yoksa paymentPageUrl url'sini biz oluşturmalıyız veya form content yüklemeliyiz.
                        // Şimdilik Ozan'ın yapısına uyup paymentPageUrl alanına doğrudan HTML content'i verebiliriz ve WebView içinde loadData kullanabiliriz,
                        // Ya da `paymentPageUrl` alanını `checkoutFormContent` ile besleyip WebViewScreen'de ona göre yükleriz.
                        _state.update { it.copy(isLoading = false, paymentPageUrl = data.checkoutFormContent ?: ("https://sandbox-api.iyzipay.com/checkout/form/initialize/" + data.token)) }
                    }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                    resolve(IyzicoPaymentWebViewContract.Effect.ShowPaymentFailed(result.message))
                }
            }
        }
    }

    private fun handleCallbackUrlReached() {
        val token = checkoutToken ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = paymentRepository.getCheckoutFormResult(token)) {
                is NetworkResult.Success -> {
                    val payment = result.data
                    if (payment.paymentStatus == "SUCCESS") {
                        resolve(IyzicoPaymentWebViewContract.Effect.ShowPaymentSucceeded(payment.paymentId.orEmpty()))
                    } else {
                        resolve(IyzicoPaymentWebViewContract.Effect.ShowPaymentFailed(payment.paymentStatus ?: "Ödeme başarısız."))
                    }
                }
                is NetworkResult.Error -> resolve(IyzicoPaymentWebViewContract.Effect.ShowPaymentFailed(result.message))
            }
        }
    }

    private fun handleDismissed() {
        resolve(IyzicoPaymentWebViewContract.Effect.ShowPaymentCancelled)
    }

    private fun resolve(effect: IyzicoPaymentWebViewContract.Effect) {
        if (hasResolved) return
        hasResolved = true
        sendEffect(effect)
    }

    private fun sendEffect(effect: IyzicoPaymentWebViewContract.Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
