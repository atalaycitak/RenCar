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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.ui.components.RenCarMapMarker
import com.example.rencar_pair.presentation.ui.components.RenCarMap
import com.example.rencar_pair.presentation.ui.components.VehicleDetailBottomSheet
import com.example.rencar_pair.presentation.ui.components.RenCarBottomNavigation
import com.example.rencar_pair.presentation.ui.components.BottomNavRoute
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onVehicleDetails: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        viewModel.onIntent(HomeIntent.LocationPermissionChanged(grants.values.any { it }))
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val fineGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarseGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted || coarseGranted) {
                viewModel.onIntent(HomeIntent.LocationPermissionChanged(true))
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    HomeScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onVehicleDetails = onVehicleDetails,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToProfile = onNavigateToProfile
    )
}

@Composable
fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onVehicleDetails: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        bottomBar = {
            RenCarBottomNavigation(
                currentRoute = BottomNavRoute.HOME,
                onNavigate = { route ->
                    when (route) {
                        BottomNavRoute.HISTORY -> onNavigateToHistory()
                        BottomNavRoute.PROFILE -> onNavigateToProfile()
                        else -> {}
                    }
                }
            )
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
                // If a vehicle is selected, center on it. Otherwise center on user, or fallback to first vehicle.
                val centerLat = state.selectedVehicle?.latitude ?: state.userLocation?.latitude ?: state.vehicles.firstOrNull()?.latitude ?: 41.0082
                val centerLng = state.selectedVehicle?.longitude ?: state.userLocation?.longitude ?: state.vehicles.firstOrNull()?.longitude ?: 28.9784
                
                val mapMarkers = remember(state.vehicles) {
                    state.vehicles.map { vehicle ->
                        RenCarMapMarker(
                            id = vehicle.id,
                            latitude = vehicle.latitude,
                            longitude = vehicle.longitude,
                            title = vehicle.title,
                            snippet = "${vehicle.pricePerDay.toInt()} TL/gün"
                        )
                    }
                }
                
                RenCarMap(
                    modifier = Modifier.fillMaxSize(),
                    latitude = centerLat,
                    longitude = centerLng,
                    zoom = 13.0,
                    userLatitude = state.userLocation?.latitude,
                    userLongitude = state.userLocation?.longitude,
                    markers = mapMarkers,
                    onMarkerClick = { id ->
                        onIntent(HomeIntent.SelectVehicle(id))
                    }
                )

                if (!state.locationPermissionGranted) {
                    PermissionNotice(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    )
                }

                // My Location FAB
                FloatingActionButton(
                    onClick = { onIntent(HomeIntent.FetchUserLocation) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Konumuma Git"
                    )
                }
            }

            VehiclePanel(
                state = state,
                onSelect = { onIntent(HomeIntent.SelectVehicle(it)) },
                onVehicleDetails = onVehicleDetails
            )
            
            // Show Bottom Sheet if a vehicle is selected from the map
            state.selectedVehicle?.let { vehicle ->
                VehicleDetailBottomSheet(
                    vehicle = vehicle,
                    onDismissRequest = { onIntent(HomeIntent.SelectVehicle(null)) },
                    onRentClick = {
                        onIntent(HomeIntent.SelectVehicle(null))
                        onVehicleDetails(vehicle.id)
                    }
                )
            }
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
            text = "Konum izni verilmedi. Araçları varsayılan İstanbul konumunda gösteriyoruz.",
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
            text = "Yakındaki araçlar",
            style = MaterialTheme.typography.titleLarge
        )
        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.vehicles.isEmpty()) {
            Text(
                text = "Yakında araç bulunamadı. Lütfen daha sonra tekrar deneyin.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.vehicles, key = { it.id }) { vehicle ->
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
                Text(text = "${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium)
                Text(text = "${vehicle.plate} - ${vehicle.type}", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${vehicle.pricePerDay.toInt()} TL/gün")
                TextButton(onClick = onDetailsClick) {
                    Text(text = "Detay")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    RenCarTheme {
        HomeScreenContent(
            state = HomeState(
                isLoading = false,
                vehicles = listOf(
                    Vehicle(
                        id = "1",
                        brand = "Renault",
                        model = "Clio",
                        plate = "34 ABC 123",
                        type = com.example.rencar_pair.domain.model.VehicleType.Sedan,
                        status = com.example.rencar_pair.domain.model.VehicleStatus.Available,
                        pricePerDay = 600.0,
                        latitude = 41.0082,
                        longitude = 28.9784
                    ),
                    Vehicle(
                        id = "2",
                        brand = "Fiat",
                        model = "Egea",
                        plate = "34 DEF 456",
                        type = com.example.rencar_pair.domain.model.VehicleType.Sedan,
                        status = com.example.rencar_pair.domain.model.VehicleStatus.Available,
                        pricePerDay = 550.0,
                        latitude = 41.0092,
                        longitude = 28.9794
                    )
                ),
                locationPermissionGranted = true
            ),
            onIntent = {},
            onVehicleDetails = {},
            onNavigateToHistory = {},
            onNavigateToProfile = {}
        )
    }
}
