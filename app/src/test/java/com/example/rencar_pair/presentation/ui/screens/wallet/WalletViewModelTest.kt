package com.example.rencar_pair.presentation.ui.screens.wallet

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.domain.repository.PaymentRepository
import com.example.rencar_pair.domain.repository.WalletRepository
import com.example.rencar_pair.domain.usecase.PaymentUseCases
import com.example.rencar_pair.data.remote.dto.CheckoutFormInitializeResponse
import com.example.rencar_pair.data.remote.dto.IyzicoPaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class WalletViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WalletViewModel
    private lateinit var mockWalletRepo: MockWalletRepository
    private lateinit var mockPaymentRepo: MockPaymentRepository

    private fun setupViewModel() {
        mockWalletRepo = MockWalletRepository()
        mockPaymentRepo = MockPaymentRepository()
        val paymentUseCases = PaymentUseCases(mockWalletRepo, mockPaymentRepo)
        viewModel = WalletViewModel(paymentUseCases)
    }

    @Test
    fun `LoadWallet success populates walletInfo and savedCards`() = runTest {
        setupViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1500.0, state.walletInfo?.balance)
        assertEquals(1, state.savedCards.size)
        assertEquals("card_token_1", state.selectedCardToken)
    }

    @Test
    fun `LoadWallet network error shows error effect`() = runTest {
        setupViewModel()
        mockWalletRepo.shouldReturnError = true

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(null, state.walletInfo)
    }

    @Test
    fun `SubmitTopUp with valid amount updates walletInfo`() = runTest {
        setupViewModel()
        advanceUntilIdle() // let it load

        viewModel.onIntent(WalletIntent.ShowTopUpDialog)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isTopUpDialogVisible)

        viewModel.onIntent(WalletIntent.UpdateTopUpAmount("500"))
        advanceUntilIdle()
        assertEquals("500", viewModel.state.value.topUpAmount)

        viewModel.onIntent(WalletIntent.SubmitTopUp)
        
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isToppingUp)
        assertFalse(state.isTopUpDialogVisible)
        assertEquals(2000.0, state.walletInfo?.balance)
    }
    
    @Test
    fun `SubmitTopUp with invalid amount does not call repository`() = runTest {
        setupViewModel()
        advanceUntilIdle() // let it load

        viewModel.onIntent(WalletIntent.ShowTopUpDialog)
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.UpdateTopUpAmount("0"))
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.SubmitTopUp)
        
        advanceUntilIdle()

        // It should reject it and keep topup dialog open
        assertTrue(viewModel.state.value.isTopUpDialogVisible)
        // Balance shouldn't change
        assertEquals(1500.0, viewModel.state.value.walletInfo?.balance)
    }

    @Test
    fun `SubmitCard with invalid data shows error`() = runTest {
        setupViewModel()
        advanceUntilIdle()

        viewModel.onIntent(WalletIntent.ShowAddCardDialog)
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.UpdateCardHolderName("John Doe"))
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.UpdateCardNumber("123")) // Invalid length
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.SubmitCard)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isAddCardDialogVisible)
        assertEquals("Kart numarası 16 haneli olmalı", state.cardFormError)
    }

    @Test
    fun `SubmitCard with valid data adds card and closes dialog`() = runTest {
        setupViewModel()
        advanceUntilIdle()

        viewModel.onIntent(WalletIntent.ShowAddCardDialog)
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.UpdateCardHolderName("John Doe"))
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.UpdateCardNumber("1234567812345678"))
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.UpdateCardExpiry("12/28"))
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.UpdateCardCvc("123"))
        advanceUntilIdle()
        viewModel.onIntent(WalletIntent.SubmitCard)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isAddCardDialogVisible)
        assertTrue(state.savedCards.size >= 2)
    }
}

class MockWalletRepository : WalletRepository {
    var shouldReturnError = false
    var balance = 1500.0

    override suspend fun getWalletInfo(): NetworkResult<WalletInfo> {
        if (shouldReturnError) return NetworkResult.Error("API Error")
        return NetworkResult.Success(WalletInfo(balance = balance, transactions = emptyList()))
    }

    override suspend fun getBalance(): NetworkResult<Double> {
        if (shouldReturnError) return NetworkResult.Error("API Error")
        return NetworkResult.Success(balance)
    }

    override suspend fun topUp(amount: Double): NetworkResult<WalletInfo> {
        if (shouldReturnError) return NetworkResult.Error("API Error")
        balance += amount
        return NetworkResult.Success(WalletInfo(balance = balance, transactions = emptyList()))
    }

    override suspend fun topUp(amount: Double, cardToken: String): NetworkResult<WalletInfo> {
        if (shouldReturnError) return NetworkResult.Error("API Error")
        balance += amount
        return NetworkResult.Success(WalletInfo(balance = balance, transactions = emptyList()))
    }
}

class MockPaymentRepository : PaymentRepository {
    var shouldReturnError = false
    private val cards = mutableListOf(
        SavedCard(cardToken = "card_token_1", cardAlias = "My Card", binNumber = "123456", cardAssociation = "VISA", isDefault = true)
    )

    override suspend fun getSavedCards(): NetworkResult<List<SavedCard>> {
        if (shouldReturnError) return NetworkResult.Error("API Error")
        return NetworkResult.Success(cards)
    }

    override suspend fun setDefaultCard(cardId: String): NetworkResult<SavedCard> {
        if (shouldReturnError) return NetworkResult.Error("API Error")
        val index = cards.indexOfFirst { it.cardToken == cardId }
        if (index != -1) {
            val updated = cards[index].copy(isDefault = true)
            cards[index] = updated
            return NetworkResult.Success(updated)
        }
        return NetworkResult.Error("Not found")
    }

    override suspend fun deleteCard(cardId: String): NetworkResult<Unit> {
        return NetworkResult.Success(Unit)
    }

    override suspend fun addCard(cardNumber: String, expireMonth: String, expireYear: String, cvc: String, cardHolderName: String): NetworkResult<SavedCard> {
        if (shouldReturnError) return NetworkResult.Error("API Error")
        val newCard = SavedCard(cardToken = "new_token", cardAlias = "New Card", binNumber = cardNumber.take(6), cardAssociation = "MASTER", isDefault = false)
        cards.add(newCard)
        return NetworkResult.Success(newCard)
    }

    override suspend fun payRental(rentalId: String, method: PaymentMethod, cardId: String?, discountCode: String?, iyzicoPaymentId: String?): NetworkResult<PaymentResult> {
        return NetworkResult.Success(PaymentResult())
    }

    override suspend fun processPayment(rentalId: String, cardToken: String, amount: Double): NetworkResult<PaymentResult> {
        return NetworkResult.Success(PaymentResult())
    }

    override suspend fun initializeCheckoutForm(price: Double, description: String?, basketId: String?): NetworkResult<CheckoutFormInitializeResponse> {
        return NetworkResult.Error("Not implemented")
    }

    override suspend fun getCheckoutFormResult(token: String): NetworkResult<IyzicoPaymentResponse> {
        return NetworkResult.Error("Not implemented")
    }
}

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
