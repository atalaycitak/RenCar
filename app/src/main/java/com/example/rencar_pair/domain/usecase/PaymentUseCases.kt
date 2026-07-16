package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.PaymentRepository
import com.example.rencar_pair.domain.repository.WalletRepository

class PaymentUseCases(
    private val walletRepository: WalletRepository,
    private val paymentRepository: PaymentRepository
) {
    suspend fun getWalletInfo() = walletRepository.getWalletInfo()
    suspend fun getWalletBalance() = walletRepository.getBalance()
    suspend fun topUpWallet(amount: Double, cardToken: String) = walletRepository.topUp(amount, cardToken)
    suspend fun getSavedCards() = paymentRepository.getSavedCards()
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
    suspend fun processPayment(rentalId: String, cardToken: String, amount: Double) =
        paymentRepository.processPayment(rentalId, cardToken, amount)
}
