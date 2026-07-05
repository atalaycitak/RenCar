package com.example.rencar_pair.presentation.ui.screens.trip_summary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.Blue50
import com.example.rencar_pair.ui.theme.Error50
import com.example.rencar_pair.ui.theme.Neutral10
import com.example.rencar_pair.ui.theme.Neutral90
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@Composable
fun TripSummaryScreen(
    rentalId: String,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripSummaryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(rentalId) {
        viewModel.onIntent(TripSummaryIntent.LoadSummary(rentalId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TripSummaryEffect.NavigateToHome -> onNavigateToHome()
                is TripSummaryEffect.ShowPaymentSuccess -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
                is TripSummaryEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        TripSummaryScreenContent(
            state = state,
            onIntent = viewModel::onIntent,
            onNavigateToHome = onNavigateToHome,
            modifier = modifier.padding(padding)
        )
    }
}

@Composable
fun TripSummaryScreenContent(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral90)
            .padding(24.dp)
    ) {
        Text(
            text = "Yolculuk Özeti",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral10
        )
        Spacer(modifier = Modifier.height(16.dp))

        state.errorMessage?.let {
            Text(
                text = it,
                color = Error50,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReceiptRow(
                    label = "Araç Bedeli",
                    value = "₺${"%.2f".format(state.rental?.totalPrice ?: 0.0)}"
                )
                ReceiptRow(label = "İndirim", value = "-₺0.00", valueColor = Color(0xFF10B981))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Toplam Tutar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "₺${"%.2f".format(state.rental?.totalPrice ?: 0.0)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Ödeme Yöntemi", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.savedCards.isEmpty()) {
                item {
                    Text(
                        "Kayitli odeme yontemi bulunamadi.",
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                items(state.savedCards, key = { it.cardToken }) { card ->
                    PaymentCardItem(
                        card = card,
                        isSelected = state.selectedCardToken == card.cardToken,
                        onClick = { onIntent(TripSummaryIntent.SelectCard(card.cardToken)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isPaying) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            PrimaryButton(
                text = "Ödemeyi Tamamla",
                onClick = { onIntent(TripSummaryIntent.Pay) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, valueColor: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PaymentCardItem(
    card: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Blue50 else Color(0xFFE2E8F0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            card.cardAssociation,
            fontWeight = FontWeight.Bold,
            color = Blue50,
            modifier = Modifier.width(60.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(card.cardAlias, fontWeight = FontWeight.Medium)
            Text("**** **** **** ${card.binNumber}", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        RadioButton(selected = isSelected, onClick = onClick)
    }
}

@Preview(showBackground = true)
@Composable
private fun TripSummaryScreenPreview() {
    RenCarTheme {
        TripSummaryScreenContent(
            state = TripSummaryState(
                rental = Rental(
                    id = "RNT-12345",
                    userId = "USR-1",
                    vehicleId = "VHC-1",
                    startDate = Instant.now(),
                    endDate = Instant.now().plusSeconds(86400),
                    status = RentalStatus.Completed,
                    totalPrice = 1500.0
                ),
                savedCards = listOf(
                    PaymentMethod(
                        cardToken = "token1",
                        cardAlias = "İş Bankası Kartım",
                        binNumber = "123456",
                        cardAssociation = "VISA"
                    )
                ),
                selectedCardToken = "token1",
                isLoading = false,
                isPaying = false
            ),
            onIntent = {},
            onNavigateToHome = {}
        )
    }
}
