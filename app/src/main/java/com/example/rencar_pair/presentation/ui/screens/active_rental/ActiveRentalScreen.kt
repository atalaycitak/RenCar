package com.example.rencar_pair.presentation.ui.screens.active_rental

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.Neutral10
import com.example.rencar_pair.ui.theme.Neutral90
import com.example.rencar_pair.ui.theme.Blue50
import com.example.rencar_pair.ui.theme.Error50
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActiveRentalScreen(
    state: ActiveRentalState,
    onIntent: (ActiveRentalIntent) -> Unit,
    onNavigateToSummary: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral90)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aktif Sürüş",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral10
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.errorMessage?.let {
            Text(
                text = it,
                color = Error50,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Geçen Süre",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "${state.elapsedMinutes} Dk",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue50
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gidilen Mesafe", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "%.1f km".format(state.distanceKm),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Güncel Tutar", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "₺%.2f".format(state.currentCost),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (state.isFinishing) {
            CircularProgressIndicator(color = Error50)
        } else {
            PrimaryButton(
                text = "Sürüşü Bitir",
                onClick = { onIntent(ActiveRentalIntent.FinishRental) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ActiveRentalRoute(
    rentalId: String,
    onNavigateToSummary: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveRentalViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(rentalId) {
        viewModel.onIntent(ActiveRentalIntent.LoadRental(rentalId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActiveRentalEffect.NavigateToSummary -> onNavigateToSummary(effect.rentalId)
                is ActiveRentalEffect.ShowError -> { /* error shown via state.errorMessage */ }
            }
        }
    }

    ActiveRentalScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToSummary = onNavigateToSummary
    )
}
