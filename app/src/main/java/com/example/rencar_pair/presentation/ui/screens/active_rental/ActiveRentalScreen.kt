package com.example.rencar_pair.presentation.ui.screens.active_rental

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.R
import com.example.rencar_pair.ui.theme.RenCarTheme
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
            onIntent = viewModel::onIntent,
            onNavigateToReturnVehicle = onNavigateToReturnVehicle
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
    onNavigateToReturnVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE9EDF2)) // Placeholder for Map background
    ) {
        // Map Placeholder Graphic
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_dialog_map),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(120.dp)
            )
        }

        // Top Status Pill
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(30.dp), spotColor = Color.Black.copy(alpha = 0.25f))
                .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(30.dp))
                .padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF34C98A), CircleShape)
            )
            Text(
                text = "Kiralama aktif · Renault Clio",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background
                )
            )
        }

        // Central Car Icon / Marker (Mock)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(34.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_directions), // Car
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        // Bottom Info Sheet
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(elevation = 40.dp, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), spotColor = Color.Black.copy(alpha = 0.14f))
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Drag Handle Indicator
                Box(
                    modifier = Modifier
                        .padding(bottom = 18.dp)
                        .size(width = 42.dp, height = 5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(3.dp))
                )

                // Timer
                Text(
                    text = "Geçen süre",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                val hours = state.elapsedMinutes / 60
                val minutes = state.elapsedMinutes % 60
                val formattedTime = String.format("%02d:%02d:00", hours, minutes) // Mock seconds as 00
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Cards (Cost & Distance)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    // Cost Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                            .padding(13.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Anlık ücret",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "₺${String.format("%.2f", state.currentCost)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Distance Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                            .padding(13.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mesafe",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "${String.format("%.1f", state.distanceKm)} km",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Actions (Lock/Unlock & End Rental)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 12.dp), // Extra padding for safe area
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    // Lock / Unlock Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .border(1.7.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .clickable { /* Toggle Lock */ },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_lock_lock), // Lock
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Kilitle / Aç",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    // End Rental Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .shadow(elevation = 24.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xFFE5484D).copy(alpha = 0.3f))
                            .background(Color(0xFFE5484D), RoundedCornerShape(16.dp))
                            .clickable { if (!state.isFinishing) onIntent(ActiveRentalIntent.FinishRental) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (state.isFinishing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Kiralamayı Bitir",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActiveRentalScreenPreview() {
    RenCarTheme {
        ActiveRentalScreenContent(
            state = ActiveRentalState(
                rental = null,
                elapsedMinutes = 24,
                distanceKm = 12.4,
                currentCost = 108.0,
                isFinishing = false,
                errorMessage = null
            ),
            onIntent = {},
            onNavigateToReturnVehicle = {}
        )
    }
}
