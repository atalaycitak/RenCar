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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.Blue50
import com.example.rencar_pair.ui.theme.Error50
import com.example.rencar_pair.ui.theme.Neutral10
import com.example.rencar_pair.ui.theme.Neutral90
import org.koin.androidx.compose.koinViewModel

@Composable
fun TripSummaryScreen(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
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
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Odeme kartlari ve islem sonucu lokal demo state uzerinden calisir; Swagger'da payment endpoint'i yok.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Receipt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReceiptRow(label = "Araç Bedeli", value = "₺${state.rental?.totalPrice ?: 0.0}")
                ReceiptRow(label = "İndirim", value = "-₺0.00", valueColor = Color(0xFF10B981))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Toplam Tutar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("₺${state.rental?.totalPrice ?: 0.0}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
        Text(card.cardAssociation, fontWeight = FontWeight.Bold, color = Blue50, modifier = Modifier.width(60.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(card.cardAlias, fontWeight = FontWeight.Medium)
            Text("**** **** **** ${card.binNumber}", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
fun TripSummaryRoute(
    rentalId: String,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripSummaryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(rentalId) {
        viewModel.onIntent(TripSummaryIntent.LoadSummary(rentalId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TripSummaryEffect.NavigateToHome -> onNavigateToHome()
                is TripSummaryEffect.ShowError -> {}
                is TripSummaryEffect.ShowPaymentSuccess -> {}
            }
        }
    }

    TripSummaryScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToHome = onNavigateToHome,
        onShowMessage = {}
    )
}
