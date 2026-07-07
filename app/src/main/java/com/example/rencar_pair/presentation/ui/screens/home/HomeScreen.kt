package com.example.rencar_pair.presentation.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.presentation.ui.components.BottomNavRoute
import com.example.rencar_pair.presentation.ui.components.RenCarBottomNavigation
import com.example.rencar_pair.presentation.ui.components.RenCarMap
import com.example.rencar_pair.presentation.ui.components.RenCarMapDefaults
import com.example.rencar_pair.presentation.ui.components.RenCarMapMarker
import com.example.rencar_pair.presentation.ui.components.VehicleDetailBottomSheet
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
    val locationPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    var locationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        viewModel.onIntent(HomeIntent.LocationPermissionChanged(grants.values.any { it }))
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    fun hasLocationPermission(): Boolean {
        return locationPermissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestLocationPermission() {
        locationPermissionRequested = true
        permissionLauncher.launch(locationPermissions)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver

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
            } else if (!locationPermissionRequested) {
                requestLocationPermission()
            } else {
                viewModel.onIntent(HomeIntent.LocationPermissionChanged(false))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            viewModel.onIntent(HomeIntent.LocationPermissionChanged(true))
        } else if (!locationPermissionRequested) {
            requestLocationPermission()
        } else {
            viewModel.onIntent(HomeIntent.LocationPermissionChanged(false))
        }
    }

    HomeScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onRequestLocationPermission = ::requestLocationPermission,
        onVehicleDetails = onVehicleDetails,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToProfile = onNavigateToProfile
    )
}

@Composable
fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onRequestLocationPermission: () -> Unit,
    onVehicleDetails: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val visibleVehicles = state.filteredVehicles

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
                    .height(380.dp)
            ) {
                val centerLat = state.selectedVehicle?.latitude
                    ?: state.userLocation?.latitude
                    ?: visibleVehicles.firstOrNull()?.latitude
                    ?: RenCarMapDefaults.DefaultLatitude
                val centerLng = state.selectedVehicle?.longitude
                    ?: state.userLocation?.longitude
                    ?: visibleVehicles.firstOrNull()?.longitude
                    ?: RenCarMapDefaults.DefaultLongitude

                val mapMarkers = remember(visibleVehicles) {
                    visibleVehicles.map { vehicle ->
                        RenCarMapMarker(
                            id = vehicle.id,
                            latitude = vehicle.latitude,
                            longitude = vehicle.longitude,
                            title = vehicle.title,
                            snippet = "${vehicle.pricePerDay.toInt()} TL/gun"
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

                MapFilterBar(
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                )

                if (!state.locationPermissionGranted) {
                    PermissionNotice(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(start = 16.dp, end = 16.dp, top = 122.dp)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        if (state.locationPermissionGranted) {
                            onIntent(HomeIntent.FetchUserLocation)
                        } else {
                            onRequestLocationPermission()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Konumuma git"
                    )
                }
            }

            VehiclePanel(
                state = state,
                visibleVehicles = visibleVehicles,
                onSelect = { onIntent(HomeIntent.SelectVehicle(it)) },
                onVehicleDetails = onVehicleDetails
            )

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
private fun MapFilterBar(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipRow(
                options = vehicleTypeOptions,
                selectedValue = state.selectedVehicleType,
                onSelected = { onIntent(HomeIntent.UpdateVehicleTypeFilter(it)) }
            )
            FilterChipRow(
                options = priceOptions,
                selectedValue = state.maxDailyPrice,
                onSelected = { onIntent(HomeIntent.UpdateMaxPriceFilter(it)) }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rangeOptions.forEach { option ->
                    FilterChip(
                        selected = state.minRangeKm == option.value,
                        onClick = { onIntent(HomeIntent.UpdateMinRangeFilter(option.value)) },
                        label = { Text(option.label) }
                    )
                }
                if (state.hasActiveFilters) {
                    TextButton(onClick = { onIntent(HomeIntent.ClearFilters) }) {
                        Text("Temizle")
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> FilterChipRow(
    options: List<FilterOption<T>>,
    selectedValue: T?,
    onSelected: (T?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedValue == option.value,
                onClick = { onSelected(option.value) },
                label = { Text(option.label) }
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
            text = "Konum izni verilmedi. Araclar varsayilan Istanbul konumunda gosteriliyor.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun VehiclePanel(
    state: HomeState,
    visibleVehicles: List<Vehicle>,
    onSelect: (String) -> Unit,
    onVehicleDetails: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Yakindaki araclar (${visibleVehicles.size})",
            style = MaterialTheme.typography.titleLarge
        )
        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (visibleVehicles.isEmpty()) {
            Text(
                text = if (state.hasActiveFilters) {
                    "Bu filtrelere uygun arac bulunamadi."
                } else {
                    "Yakinda arac bulunamadi. Lutfen daha sonra tekrar deneyin."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibleVehicles, key = { it.id }) { vehicle ->
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
                Text(text = "${vehicle.rangeKm} km menzil", style = MaterialTheme.typography.bodySmall)
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

private data class FilterOption<T>(
    val value: T?,
    val label: String
)

private val vehicleTypeOptions = listOf(
    FilterOption<VehicleType>(null, "Tum tipler"),
    FilterOption(VehicleType.Sedan, "Sedan"),
    FilterOption(VehicleType.Suv, "SUV"),
    FilterOption(VehicleType.Hatchback, "Hatchback"),
    FilterOption(VehicleType.Minivan, "Minivan")
)

private val priceOptions = listOf(
    FilterOption<Int>(null, "Tum fiyatlar"),
    FilterOption(1000, "1000 TL alti"),
    FilterOption(1500, "1500 TL alti"),
    FilterOption(2500, "2500 TL alti"),
    FilterOption(4000, "4000 TL alti")
)

private val rangeOptions = listOf(
    FilterOption<Int>(null, "Tum menziller"),
    FilterOption(300, "300+ km"),
    FilterOption(400, "400+ km"),
    FilterOption(500, "500+ km")
)

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
                        type = VehicleType.Sedan,
                        status = VehicleStatus.Available,
                        pricePerDay = 600.0,
                        latitude = 41.0082,
                        longitude = 28.9784,
                        rangeKm = 420
                    ),
                    Vehicle(
                        id = "2",
                        brand = "Dacia",
                        model = "Duster",
                        plate = "34 SUV 456",
                        type = VehicleType.Suv,
                        status = VehicleStatus.Available,
                        pricePerDay = 2100.0,
                        latitude = 41.0092,
                        longitude = 28.9794,
                        rangeKm = 360
                    )
                ),
                locationPermissionGranted = true
            ),
            onIntent = {},
            onRequestLocationPermission = {},
            onVehicleDetails = {},
            onNavigateToHistory = {},
            onNavigateToProfile = {}
        )
    }
}
