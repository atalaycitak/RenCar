package com.example.rencar_pair.presentation.ui.screens.active_rental

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.Blue50
import com.example.rencar_pair.ui.theme.Error50
import com.example.rencar_pair.ui.theme.Neutral10
import com.example.rencar_pair.ui.theme.Neutral90
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral90)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aktif Surus",
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
                    text = "Gecen Sure",
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
                        Text("Guncel Tutar", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "TL %.2f".format(state.currentCost),
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
                text = "Iade Surecini Baslat",
                onClick = { onIntent(ActiveRentalIntent.FinishRental) },
                modifier = Modifier.fillMaxWidth()
            )
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
                elapsedMinutes = 45,
                distanceKm = 12.5,
                currentCost = 150.0,
                isFinishing = false,
                errorMessage = null
            ),
            onIntent = {},
            onNavigateToReturnVehicle = {}
        )
    }
}
