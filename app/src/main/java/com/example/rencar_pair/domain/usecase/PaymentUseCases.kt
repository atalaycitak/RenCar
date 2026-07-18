package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.repository.PaymentRepository
import com.example.rencar_pair.domain.repository.WalletRepository

class PaymentUseCases(
    private val walletRepository: WalletRepository,
    private val paymentRepository: PaymentRepository
) {
    suspend fun getWalletInfo() = walletRepository.getWalletInfo()
    suspend fun getWalletBalance() = walletRepository.getBalance()
    suspend fun topUpWallet(amount: Double) = walletRepository.topUp(amount)
    suspend fun topUpWallet(amount: Double, cardToken: String) = walletRepository.topUp(amount, cardToken)
    suspend fun getSavedCards() = paymentRepository.getSavedCards()
    suspend fun setDefaultCard(cardId: String) = paymentRepository.setDefaultCard(cardId)
    suspend fun deleteCard(cardId: String) = paymentRepository.deleteCard(cardId)

    suspend fun addCard(
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        cardHolderName: String
    ) = paymentRepository.addCard(
        cardNumber = cardNumber,
        expireMonth = expireMonth,
        expireYear = expireYear,
        cvc = cvc,
        cardHolderName = cardHolderName
    )

    suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String? = null,
        discountCode: String? = null,
        iyzicoPaymentId: String? = null
    ) = paymentRepository.payRental(
        rentalId = rentalId,
        method = method,
        cardId = cardId,
        discountCode = discountCode,
        iyzicoPaymentId = iyzicoPaymentId
    )

    suspend fun processPayment(rentalId: String, cardToken: String, amount: Double) =
        paymentRepository.processPayment(rentalId, cardToken, amount)
}
