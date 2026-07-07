package com.example.rencar_pair.presentation.ui.screens.active_rental

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.components.RenCarMap
import com.example.rencar_pair.presentation.ui.components.RenCarMapDefaults
import com.example.rencar_pair.presentation.ui.components.RenCarMapMarker
import com.example.rencar_pair.ui.theme.RenCarTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActiveRentalScreen(
    rentalId: String,
    onNavigateToReturnVehicle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveRentalViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(rentalId) {
        viewModel.onIntent(ActiveRentalIntent.LoadRental(rentalId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToReturnVehicle -> onNavigateToReturnVehicle(effect.rentalId)
                is ActiveRentalEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Box(modifier = modifier) {
        ActiveRentalScreenContent(
            state = state,
            onIntent = viewModel::onIntent
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ActiveRentalScreenContent(
    state: ActiveRentalState,
    onIntent: (ActiveRentalIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (state.isLoading && state.rental == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Aktif kiralama",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                state.errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                ActiveVehicleMap(vehicle = state.vehicle)

                ActiveVehicleCard(state = state)

                RentalProgressCard(state = state)

                ReturnPolicyCard(rental = state.rental)

                if (state.isFinishing) {
                    CircularProgressIndicator()
                } else {
                    PrimaryButton(
                        text = "İade sürecini başlat",
                        onClick = { onIntent(ActiveRentalIntent.FinishRental) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.rental != null
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveVehicleMap(vehicle: Vehicle?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        val latitude = vehicle?.latitude ?: RenCarMapDefaults.DefaultLatitude
        val longitude = vehicle?.longitude ?: RenCarMapDefaults.DefaultLongitude
        RenCarMap(
            modifier = Modifier.fillMaxSize(),
            latitude = latitude,
            longitude = longitude,
            zoom = 14.0,
            markers = vehicle?.let {
                listOf(
                    RenCarMapMarker(
                        id = it.id,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        title = it.title,
                        snippet = "${it.plate} • ${it.locationName}",
                        selected = true
                    )
                )
            } ?: emptyList()
        )
    }
}

@Composable
private fun ActiveVehicleCard(state: ActiveRentalState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val vehicle = state.vehicle
            Text(
                text = vehicle?.title ?: "Araç bilgisi yükleniyor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = vehicle?.plate ?: state.rental?.vehicleId.orEmpty())
            if (vehicle != null) {
                Text(
                    text = "${vehicle.locationName} • ${vehicle.rangeKm} km şarj menzili",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RentalProgressCard(state: ActiveRentalState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = "Sürüş özeti", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "Geçen süre",
                    value = "${state.elapsedMinutes} dk",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Tahmini ücret",
                    value = state.currentCost.formatCurrency(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "Gidilen mesafe",
                    value = "%.1f km".format(state.distanceKm),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Durum",
                    value = state.rental?.status?.name ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReturnPolicyCard(rental: Rental?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "İade bilgisi", style = MaterialTheme.typography.titleMedium)
            Text(
                text = rental?.let { "Planlanan iade: ${it.endDate.formatRentalDate()}" }
                    ?: "Kiralama bilgisi yükleniyor",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "İade sürecini başlatınca araç fotoğrafları ve hasar notu ile teslim adımına geçilir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val rentalDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")
        .withLocale(Locale.forLanguageTag("tr-TR"))
        .withZone(ZoneId.systemDefault())

private fun Instant.formatRentalDate(): String {
    return rentalDateFormatter.format(this)
}

private fun Double.formatCurrency(): String {
    return "₺${"%.2f".format(this)}"
}

@Preview(showBackground = true)
@Composable
private fun ActiveRentalScreenPreview() {
    RenCarTheme {
        ActiveRentalScreenContent(
            state = ActiveRentalState(
                rental = Rental(
                    id = "rental-1",
                    userId = "user-1",
                    vehicleId = "vehicle-1",
                    startDate = Instant.now().minusSeconds(2700),
                    endDate = Instant.now().plusSeconds(86400),
                    totalPrice = 1200.0,
                    status = RentalStatus.Active
                ),
                vehicle = Vehicle(
                    id = "vehicle-1",
                    plate = "34 ABC 123",
                    brand = "Renault",
                    model = "Clio",
                    type = VehicleType.Hatchback,
                    pricePerDay = 1200.0,
                    status = VehicleStatus.Available,
                    latitude = 41.0082,
                    longitude = 28.9784,
                    rangeKm = 420,
                    locationName = "Sultanahmet, İstanbul"
                ),
                elapsedMinutes = 45,
                distanceKm = 12.5,
                currentCost = 1312.5,
                isFinishing = false,
                errorMessage = null
            ),
            onIntent = {}
        )
    }
}
