package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.ReservationQuote
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.components.RenCarTopBar
import com.example.rencar_pair.ui.theme.RenCarTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
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
        VehicleSummaryCard(vehicle = vehicle)

        DurationPicker(
            selectedDays = state.selectedDays,
            onIntent = onIntent
        )

        ReservationDateCard(quote = quote)

        QuoteCard(quote = quote)

        CancellationPolicyCard()

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        PrimaryButton(
            text = if (state.isSubmitting) "Rezervasyon oluşturuluyor" else "Rezervasyonu onayla",
            onClick = { onIntent(ReservationIntent.ConfirmReservation) },
            enabled = !state.isSubmitting
        )
    }
}

@Composable
private fun VehicleSummaryCard(vehicle: Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = vehicle.title, style = MaterialTheme.typography.titleLarge)
            Text(text = vehicle.plate, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${vehicle.locationName} teslim noktası",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${vehicle.rangeKm} km şarj menzili",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DurationPicker(
    selectedDays: Int,
    onIntent: (ReservationIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Kiralama süresi", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "1-30 gün arasında seçilebilir",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onIntent(ReservationIntent.DecreaseDays) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Azalt")
                    }
                    Text(
                        text = "$selectedDays gün",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { onIntent(ReservationIntent.IncreaseDays) }) {
                        Icon(Icons.Default.Add, contentDescription = "Artır")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durationOptions.forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { onIntent(ReservationIntent.SelectDays(days)) },
                        label = { Text("$days gün") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationDateCard(quote: ReservationQuote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Tarih özeti", style = MaterialTheme.typography.titleMedium)
            QuoteRow("Teslim alma", Instant.now().formatReservationDate())
            QuoteRow("İade tarihi", quote.endDateIso.formatReservationDate())
            Text(
                text = "Kiralama isteği gerçek API'ye araç kimliği ve iade tarihi ile gönderilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
            Text(text = "Fiyat özeti", style = MaterialTheme.typography.titleMedium)
            QuoteRow("Günlük fiyat", quote.pricePerDay.formatCurrency())
            QuoteRow("Süre", "${quote.days} gün")
            QuoteRow("Ara toplam", quote.subtotal.formatCurrency())
            QuoteRow("Servis bedeli", quote.serviceFee.formatCurrency())
            QuoteRow("Teslim ücreti", quote.deliveryFee.formatCurrency())
            Spacer(modifier = Modifier.height(2.dp))
            QuoteRow("Toplam", quote.totalPrice.formatCurrency(), strong = true)
        }
    }
}

@Composable
private fun CancellationPolicyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "İptal koşulu", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Teslim saatinden 2 saat öncesine kadar ücretsiz iptal edilebilir. Teslim sonrası ücretlendirme başlar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

private val durationOptions = listOf(1, 3, 7, 14)

private val reservationDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")
        .withLocale(Locale.forLanguageTag("tr-TR"))
        .withZone(ZoneId.systemDefault())

private fun String.formatReservationDate(): String {
    return try {
        Instant.parse(this).formatReservationDate()
    } catch (_: DateTimeParseException) {
        this
    }
}

private fun Instant.formatReservationDate(): String {
    return reservationDateFormatter.format(this)
}

private fun Double.formatCurrency(): String {
    return "₺${"%.2f".format(this)}"
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
                    endDateIso = "2026-01-01T00:00:00Z",
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
