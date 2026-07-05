package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rencar_pair.domain.model.ReservationQuote
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.components.RenCarTopBar
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReservationScreen(
    onBack: () -> Unit,
    onDeliveryChecklist: (String, String) -> Unit,
    viewModel: ReservationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReservationEffect.NavigateToDelivery -> {
                    onDeliveryChecklist(effect.rentalId, effect.vehicleId)
                }
            }
        }
    }

    ReservationScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack
    )
}

@Composable
fun ReservationScreenContent(
    state: ReservationState,
    onIntent: (ReservationIntent) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { RenCarTopBar(onBackClick = onBack, title = "Rezervasyon") }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.errorMessage != null && state.vehicle == null -> Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
                state.vehicle != null && state.quote != null -> ReservationView(
                    state = state,
                    onIntent = onIntent
                )
            }
        }
    }
}

@Composable
private fun ReservationView(
    state: ReservationState,
    onIntent: (ReservationIntent) -> Unit
) {
    val vehicle = state.vehicle ?: return
    val quote = state.quote ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = vehicle.title, style = MaterialTheme.typography.titleLarge)
                Text(text = vehicle.plate, style = MaterialTheme.typography.bodyMedium)
                Text(text = "${vehicle.locationName} teslim noktası")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Kiralama süresi", style = MaterialTheme.typography.titleMedium)
                    Text(text = "1-30 gün arası seçilebilir")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onIntent(ReservationIntent.DecreaseDays) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Azalt")
                    }
                    Text(
                        text = "${state.selectedDays}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { onIntent(ReservationIntent.IncreaseDays) }) {
                        Icon(Icons.Default.Add, contentDescription = "Artır")
                    }
                }
            }
        }

        QuoteCard(quote = quote)

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Text(
            text = "Not: Fiyat hesaplama istemci tarafında fake use case ile gösterilir; backend kiralama oluştururken kendi toplam fiyatını kilitler.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PrimaryButton(
            text = if (state.isSubmitting) "Oluşturuluyor" else "Rezervasyonu onayla",
            onClick = { onIntent(ReservationIntent.ConfirmReservation) },
            enabled = !state.isSubmitting
        )
    }
}

@Composable
private fun QuoteCard(quote: ReservationQuote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuoteRow("Günlük fiyat", "${quote.pricePerDay.toInt()} TL")
            QuoteRow("Süre", "${quote.days} gün")
            QuoteRow("Servis bedeli", "${quote.serviceFee.toInt()} TL")
            QuoteRow("Teslim ücreti", "${quote.deliveryFee.toInt()} TL")
            QuoteRow("Toplam", "${quote.totalPrice.toInt()} TL", strong = true)
        }
    }
}

@Composable
private fun QuoteRow(
    label: String,
    value: String,
    strong: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(
            text = value,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservationScreenPreview() {
    RenCarTheme {
        ReservationScreenContent(
            state = ReservationState(
                isLoading = false,
                vehicle = Vehicle(
                    id = "1",
                    brand = "Renault",
                    model = "Clio",
                    plate = "34 ABC 123",
                    type = com.example.rencar_pair.domain.model.VehicleType.Sedan,
                    status = com.example.rencar_pair.domain.model.VehicleStatus.Available,
                    pricePerDay = 600.0,
                    latitude = 41.0,
                    longitude = 28.0
                ),
                quote = ReservationQuote(
                    vehicleId = "1",
                    endDateIso = "2024-01-01T00:00:00Z",
                    pricePerDay = 600.0,
                    days = 3,
                    serviceFee = 100.0,
                    deliveryFee = 50.0,
                    totalPrice = 1950.0
                )
            ),
            onIntent = {},
            onBack = {}
        )
    }
}
