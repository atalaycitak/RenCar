package com.example.rencar_pair.presentation.ui.screens.trip_summary

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.R
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.Duration

@Composable
fun TripSummaryScreen(
    rentalId: String,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripSummaryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(rentalId) {
        viewModel.onIntent(TripSummaryIntent.LoadSummary(rentalId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TripSummaryEffect.NavigateToHome -> onNavigateToHome()
                is TripSummaryEffect.ShowPaymentSuccess -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
                is TripSummaryEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        TripSummaryScreenContent(
            state = state,
            onIntent = viewModel::onIntent,
            onNavigateToHome = onNavigateToHome,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun TripSummaryScreenContent(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.rental == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = state.errorMessage ?: "Kiralama bilgileri yüklenemedi.", color = MaterialTheme.colorScheme.error)
                PrimaryButton(text = "Tekrar Dene", onClick = { onIntent(TripSummaryIntent.LoadSummary(state.rentalId ?: "")) })
            }
        }
        return
    }

    val rentalDurationMins = Duration.between(state.rental.startedAt, state.rental.endedAt ?: Instant.now()).toMinutes()

    if (state.isAddCardDialogVisible) {
        AddCardDialog(
            state = state,
            onIntent = onIntent
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Success Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(Color(0xFFE7F4EC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF1FB370), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_agenda), // Mock checkmark
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Text(
                text = "Yolculuk tamamlandı",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = "Renault Clio · 34 RNC 022", // Mocked as per design
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Time and Distance Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(13.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Süre",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "\$rentalDurationMins dk",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
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
                    text = "12,4 km", // Mock
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Receipt Card
        val totalPrice = state.rental?.totalPrice ?: 0.0
        val formattedTotalPrice = String.format(java.util.Locale.US, "%.2f", totalPrice)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            ReceiptRow(label = "Kiralama ücreti ($rentalDurationMins dk)", value = "₺$formattedTotalPrice")
            ReceiptRow(label = "Başlangıç ücreti", value = "₺15,00") // Mock
            ReceiptRow(label = "Hizmet bedeli", value = "₺7,50") // Mock
            ReceiptRow(label = "İndirim · İLKSÜRÜŞ", value = "−₺20,00", valueColor = Color(0xFF1A9E63)) // Mock
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toplam",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "₺$formattedTotalPrice",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Payment Method Card
        val selectedCard = state.savedCards.find { it.cardToken == state.selectedCardToken } ?: state.savedCards.firstOrNull()
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .clickable { onIntent(TripSummaryIntent.ShowAddCardDialog) }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 28.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedCard?.cardAssociation ?: "VISA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedCard != null) "•••• \${selectedCard.binNumber.takeLast(4)}" else "Kart seçilmedi",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = selectedCard?.cardAlias ?: "Kişisel kart",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Text(
                text = if (selectedCard == null) "Kart ekle" else "Yeni kart",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Primary Button
        val finalPrice = state.rental?.totalPrice ?: 0.0
        val formattedButtonPrice = String.format(java.util.Locale.US, "%.2f", finalPrice)
        val buttonText = if (state.isPaying) "İşleniyor..." else "₺$formattedButtonPrice Öde"
        PrimaryButton(
            text = buttonText,
            onClick = {
                if (!state.isPaying) {
                    if (state.savedCards.isEmpty()) {
                        onIntent(TripSummaryIntent.ShowAddCardDialog)
                    } else {
                        onIntent(TripSummaryIntent.Pay)
                    }
                }
            },
            enabled = !state.isPaying,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AddCardDialog(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (!state.isSavingCard) onIntent(TripSummaryIntent.HideAddCardDialog)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Kart ekle",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Bu ödeme ekranından çıkmadan kartını kaydedebilirsin.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            OutlinedTextField(
                value = state.cardHolderName,
                onValueChange = { onIntent(TripSummaryIntent.UpdateCardHolderName(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kart üzerindeki isim") },
                singleLine = true,
                enabled = !state.isSavingCard
            )
            OutlinedTextField(
                value = state.cardNumber.chunked(4).joinToString(" "),
                onValueChange = { onIntent(TripSummaryIntent.UpdateCardNumber(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kart numarası") },
                singleLine = true,
                enabled = !state.isSavingCard,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.cardExpiry,
                    onValueChange = { onIntent(TripSummaryIntent.UpdateCardExpiry(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("AA/YY") },
                    singleLine = true,
                    enabled = !state.isSavingCard,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.cardCvc,
                    onValueChange = { onIntent(TripSummaryIntent.UpdateCardCvc(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("CVC") },
                    singleLine = true,
                    enabled = !state.isSavingCard,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
            }

            state.cardFormError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onIntent(TripSummaryIntent.HideAddCardDialog) },
                    enabled = !state.isSavingCard
                ) {
                    Text("Vazgeç")
                }
                TextButton(
                    onClick = { onIntent(TripSummaryIntent.SubmitCard) },
                    enabled = !state.isSavingCard
                ) {
                    Text(if (state.isSavingCard) "Kaydediliyor..." else "Kaydet")
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TripSummaryScreenPreview() {
    RenCarTheme {
        TripSummaryScreenContent(
            state = TripSummaryState(
                rental = Rental(
                    id = "RNT-12345",
                    userId = "USR-1",
                    vehicleId = "VHC-1",
                    plan = com.example.rencar_pair.domain.model.RentalPlan.PerMinute,
                    status = RentalStatus.Completed,
                    paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Unpaid,
                    paymentMethod = null,
                    totalPrice = 110.50,
                    startFee = 15.0,
                    serviceFee = 10.5,
                    distanceKm = 3.2,
                    durationMinutes = 13.0,
                    discountAmount = 0.0,
                    startedAt = Instant.now().minus(Duration.ofMinutes(24)),
                    endedAt = Instant.now(),
                    scheduledEndDate = null,
                    createdAt = Instant.now().minus(Duration.ofMinutes(30))
                ),
                savedCards = emptyList(),
                selectedCardToken = null,
                isLoading = false,
                isPaying = false
            ),
            onIntent = {},
            onNavigateToHome = {}
        )
    }
}
