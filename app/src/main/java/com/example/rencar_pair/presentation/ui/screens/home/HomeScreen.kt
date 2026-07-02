package com.example.rencar_pair.presentation.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.ui.components.RenCarMapMarker
import com.example.rencar_pair.presentation.ui.components.RenCarMap
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    onVehicleDetails: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onVehicleDetails = onVehicleDetails
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onVehicleDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        onIntent(HomeIntent.LocationPermissionChanged(grants.values.any { it }))
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            onIntent(HomeIntent.LocationPermissionChanged(true))
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text(text = "Harita") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                val center = state.selectedVehicle
                RenCarMap(
                    modifier = Modifier.fillMaxSize(),
                    latitude = center?.latitude ?: 41.0082,
                    longitude = center?.longitude ?: 28.9784,
                    zoom = 13.0,
                    markers = state.vehicles.map { vehicle ->
                        RenCarMapMarker(
                            id = vehicle.id,
                            latitude = vehicle.latitude,
                            longitude = vehicle.longitude,
                            title = vehicle.title,
                            snippet = "${vehicle.plate} - ${vehicle.pricePerDay.toInt()} TL/gun"
                        )
                    }
                )

                if (!state.locationPermissionGranted) {
                    PermissionNotice(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    )
                }
            }

            VehiclePanel(
                state = state,
                onSelect = { onIntent(HomeIntent.SelectVehicle(it)) },
                onVehicleDetails = onVehicleDetails
            )
        }
    }
}

@Composable
private fun PermissionNotice(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = "Konum izni verilmedi. Araclari varsayilan Istanbul konumunda gosteriyoruz.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun VehiclePanel(
    state: HomeState,
    onSelect: (String) -> Unit,
    onVehicleDetails: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Yakindaki araclar",
            style = MaterialTheme.typography.titleLarge
        )
        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.vehicles) { vehicle ->
                    VehicleRow(
                        vehicle = vehicle,
                        selected = vehicle.id == state.selectedVehicle?.id,
                        onClick = { onSelect(vehicle.id) },
                        onDetailsClick = { onVehicleDetails(vehicle.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleRow(
    vehicle: Vehicle,
    selected: Boolean,
    onClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = vehicle.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "${vehicle.plate} - ${vehicle.rangeKm} km", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${vehicle.pricePerDay.toInt()} TL/gun")
                TextButton(onClick = onDetailsClick) {
                    Text(text = "Detay")
                }
            }
        }
    }
}
