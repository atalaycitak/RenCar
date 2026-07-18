package com.example.rencar_pair.presentation.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.presentation.ui.components.BottomNavRoute
import com.example.rencar_pair.presentation.ui.components.RenCarBottomNavigation
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TripHistoryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: TripHistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    TripHistoryScreenContent(
        state = state,
        onNavigateToHome = onNavigateToHome,
        onNavigateToWallet = onNavigateToWallet,
        onNavigateToProfile = onNavigateToProfile
    )
}

@Composable
fun TripHistoryScreenContent(
    state: TripHistoryState,
    onNavigateToHome: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        bottomBar = {
            RenCarBottomNavigation(
                currentRoute = BottomNavRoute.HISTORY,
                onNavigate = { route ->
                    when (route) {
                        BottomNavRoute.HOME -> onNavigateToHome()
                        BottomNavRoute.WALLET -> onNavigateToWallet()
                        BottomNavRoute.PROFILE -> onNavigateToProfile()
                        else -> {}
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 6.dp)
            ) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Kiralamalarım",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                
                val totalSpent = state.totalSpent
                Text(
                    text = "Bu ay ${state.rentals.size} yolculuk · ₺${totalSpent.toInt()} harcama",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 3.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.rentals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Henüz bir sürüşünüz bulunmuyor.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.rentals, key = { it.id }) { rental ->
                        TripHistoryCard(rental = rental)
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun TripHistoryCard(rental: Rental) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale("tr")).withZone(ZoneId.systemDefault())
    val formattedDate = formatter.format(rental.startedAt ?: rental.createdAt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        // Map Placeholder Graphic
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFE6EBF1), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFF0B6BCB).copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = rental.vehicleLabel(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                
                val formattedPrice = String.format(Locale.US, "%.2f", rental.totalPrice ?: 0.0)
                Text(
                    text = "₺$formattedPrice",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(top = 3.dp)
            )

            Row(
                modifier = Modifier.padding(top = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF1F4F8), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = rental.durationMinutes?.toInt()?.let { "$it dk" } ?: "Süre yok",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF1F4F8), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = rental.distanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "Mesafe yok",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

private fun Rental.vehicleLabel(): String {
    return "Araç #${vehicleId.takeLast(6).uppercase(Locale.US)}"
}

@Preview(showBackground = true)
@Composable
private fun TripHistoryScreenPreview() {
    RenCarTheme {
        TripHistoryScreenContent(
            state = TripHistoryState(
                isLoading = false,
                rentals = listOf(
                    Rental(
                        id = "RNT-1",
                        userId = "USR-1",
                        vehicleId = "VHC-1",
                        plan = com.example.rencar_pair.domain.model.RentalPlan.Daily,
                        status = RentalStatus.Completed,
                        paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Paid,
                        paymentMethod = com.example.rencar_pair.domain.model.PaymentMethod.Wallet,
                        totalPrice = 110.50,
                        startFee = 15.0,
                        serviceFee = 10.0,
                        distanceKm = null,
                        durationMinutes = null,
                        discountAmount = 0.0,
                        startedAt = Instant.now().minusSeconds(86400 * 2),
                        endedAt = Instant.now().minusSeconds(86400 * 1),
                        scheduledEndDate = null,
                        createdAt = Instant.now().minusSeconds(86400 * 3)
                    ),
                    Rental(
                        id = "RNT-2",
                        userId = "USR-1",
                        vehicleId = "VHC-2",
                        plan = com.example.rencar_pair.domain.model.RentalPlan.PerMinute,
                        status = RentalStatus.Active,
                        paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Unpaid,
                        paymentMethod = null,
                        totalPrice = null,
                        startFee = 15.0,
                        serviceFee = null,
                        distanceKm = null,
                        durationMinutes = null,
                        discountAmount = 0.0,
                        startedAt = Instant.now(),
                        endedAt = null,
                        scheduledEndDate = null,
                        createdAt = Instant.now()
                    )
                )
            ),
            onNavigateToHome = {},
            onNavigateToWallet = {},
            onNavigateToProfile = {}
        )
    }
}
