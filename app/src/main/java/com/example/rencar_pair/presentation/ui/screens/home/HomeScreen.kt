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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.rencar_pair.R
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.presentation.ui.components.BottomNavRoute
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.components.RenCarBottomNavigation
import com.example.rencar_pair.presentation.ui.components.RenCarMap
import com.example.rencar_pair.presentation.ui.components.RenCarMapMarker
import com.example.rencar_pair.presentation.ui.components.VehicleDetailBottomSheet
import com.example.rencar_pair.presentation.ui.components.rememberRencarMapController
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel
import org.maplibre.android.geometry.LatLng

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
    val visibleVehicles = state.filteredVehicles

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val centerLat = state.selectedVehicle?.latitude
                ?: state.userLocation?.latitude
                ?: visibleVehicles.firstOrNull()?.latitude
                ?: 41.0082
            val centerLng = state.selectedVehicle?.longitude
                ?: state.userLocation?.longitude
                ?: visibleVehicles.firstOrNull()?.longitude
                ?: 28.9784

            val mapMarkers = remember(visibleVehicles) {
                visibleVehicles.map { vehicle ->
                    RenCarMapMarker(
                        id = vehicle.id,
                        latitude = vehicle.latitude,
                        longitude = vehicle.longitude,
                        title = vehicle.title,
                        snippet = "",
                        text = "₺${vehicle.pricePerDay.toInt()}",
                        colorHex = getHexColorForVehicle(vehicle)
                    )
                }
            }

            val mapController = rememberRencarMapController()

            RenCarMap(
                myLocation = state.userLocation?.let { LatLng(it.latitude, it.longitude) },
                modifier = Modifier.fillMaxSize(),
                initialCenter = LatLng(centerLat, centerLng),
                initialZoom = 13.0,
                controller = mapController,
                markers = mapMarkers,
                onMarkerClick = { id ->
                    onIntent(HomeIntent.SelectVehicle(id))
                }
            )

            // Top Gradient for status bar readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Top Search Bar
            TopSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 54.dp, start = 18.dp, end = 18.dp)
            )

            if (!state.locationPermissionGranted) {
                PermissionNotice(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(start = 18.dp, end = 18.dp, top = 130.dp)
                )
            }

            // Bottom Navigation and Sheet Container
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // FAB container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 18.dp, bottom = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    FloatingActionButton(
                        onClick = {
                            onIntent(HomeIntent.FetchUserLocation)
                            state.userLocation?.let { loc ->
                                mapController.animateTo(LatLng(loc.latitude, loc.longitude))
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(18.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Konumuma git"
                        )
                    }
                }

                HomeBottomSheet(
                    vehicleCount = visibleVehicles.size,
                    onFindNearestClick = {
                        // Action for finding nearest
                    }
                )

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
private fun TopSearchBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(24.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Target icon
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                    drawCircle(
                        color = Color(0xFF5C6675),
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Nereden araç alacaksın?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Settings/Filter icon placeholder
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_manage),
                    contentDescription = "Filtreler",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeBottomSheet(
    vehicleCount: Int,
    onFindNearestClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 30.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(5.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Yakınında $vehicleCount araç",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 20.sp,
                            letterSpacing = (-0.4).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Kadıköy çevresinde · 3 dk uzaklıkta",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size), // list icon
                        contentDescription = "Liste görünümü",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipCustom("Tümü", null, isSelected = true)
                FilterChipCustom("Ekonomik", Color(0xFFF5821F), isSelected = false)
                FilterChipCustom("Konfor", Color(0xFF7C5CE6), isSelected = false)
                FilterChipCustom("SUV", Color(0xFFE6A700), isSelected = false)
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "En Yakın Aracı Bul",
                onClick = onFindNearestClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilterChipCustom(
    text: String,
    dotColor: Color?,
    isSelected: Boolean
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = if (isSelected) 14.dp else 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

private fun getHexColorForVehicle(vehicle: Vehicle): String {
    if (vehicle.status == VehicleStatus.Rented) {
        return "#9AA3AE"
    }
    return when (vehicle.type) {
        VehicleType.Hatchback -> "#F5821F" // Ekonomik
        VehicleType.Sedan -> "#7C5CE6" // Konfor
        VehicleType.Suv -> "#E6A700" // SUV
        VehicleType.Minivan -> "#0AB5A6"
        else -> "#0AB5A6"
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
            text = "Konum izni verilmedi. Araçlar varsayılan İstanbul konumunda gösteriliyor.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
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
                        type = VehicleType.Hatchback,
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
            onVehicleDetails = {},
            onNavigateToHistory = {},
            onNavigateToProfile = {}
        )
    }
}
