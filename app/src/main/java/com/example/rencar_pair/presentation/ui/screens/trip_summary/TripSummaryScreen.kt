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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant
import java.util.Locale

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
    when {
        state.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        state.paymentResult != null -> {
            PaymentSuccessContent(
                result = state.paymentResult,
                state = state,
                onNavigateToHome = onNavigateToHome,
                modifier = modifier
            )
            return
        }
        state.rental == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = state.errorMessage ?: "Kiralama bilgileri yüklenemedi.",
                        color = MaterialTheme.colorScheme.error
                    )
                    PrimaryButton(
                        text = "Tekrar dene",
                        onClick = { onIntent(TripSummaryIntent.LoadSummary(state.rentalId)) }
                    )
                }
            }
            return
        }
    }

    val rental = state.rental
    val startedAt = rental.startedAt ?: rental.createdAt
    val endedAt = rental.endedAt ?: Instant.now()
    val rentalDurationMins = Duration.between(startedAt, endedAt).toMinutes().coerceAtLeast(0)

    if (state.isAddCardDialogVisible) {
        AddCardDialog(state = state, onIntent = onIntent)
    }
    if (state.isTopUpDialogVisible) {
        TopUpDialog(state = state, onIntent = onIntent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TripCompleteHeader(vehicleLabel = rental.vehicleId)
        SummaryMetricRow(durationMinutes = rentalDurationMins, distanceKm = rental.distanceKm)
        ReceiptCard(state = state, durationMinutes = rentalDurationMins)
        PaymentMethodCard(state = state, onIntent = onIntent)

        if (state.walletShortfall > 0.0 && state.selectedPaymentMethod == PaymentMethod.Wallet) {
            InsufficientWalletCard(state = state, onIntent = onIntent)
        }

        val amountText = formatCurrency(state.totalPrice)
        PrimaryButton(
            text = if (state.isPaying) "İşleniyor..." else "$amountText öde",
            onClick = { onIntent(TripSummaryIntent.Pay) },
            enabled = !state.isPaying,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun TripCompleteHeader(vehicleLabel: String) {
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
                    painter = painterResource(id = android.R.drawable.checkbox_on_background),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = "Yolculuk tamamlandı",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Araç #$vehicleLabel",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun SummaryMetricRow(durationMinutes: Long, distanceKm: Double?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        MetricTile(label = "Süre", value = "$durationMinutes dk", modifier = Modifier.weight(1f))
        MetricTile(
            label = "Mesafe",
            value = distanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "-",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
private fun ReceiptCard(state: TripSummaryState, durationMinutes: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        ReceiptRow(label = "Kiralama ücreti ($durationMinutes dk)", value = formatCurrency(state.rental?.totalPrice))
        state.rental?.startFee?.let { ReceiptRow(label = "Başlangıç ücreti", value = formatCurrency(it)) }
        state.rental?.serviceFee?.let { ReceiptRow(label = "Hizmet bedeli", value = formatCurrency(it)) }
        state.rental?.discountAmount?.takeIf { it > 0.0 }?.let {
            ReceiptRow(label = "İndirim", value = "-${formatCurrency(it)}", valueColor = Color(0xFF1A9E63))
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        ReceiptRow(
            label = "Toplam",
            value = formatCurrency(state.totalPrice),
            valueColor = MaterialTheme.colorScheme.onBackground,
            isEmphasized = true
        )
    }
}

@Composable
private fun PaymentMethodCard(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Ödeme yöntemi",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        PaymentOptionRow(
            selected = state.selectedPaymentMethod == PaymentMethod.Wallet,
            title = "Cüzdan",
            subtitle = "Bakiye ${formatCurrency(state.walletBalance)}",
            onClick = { onIntent(TripSummaryIntent.SelectPaymentMethod(PaymentMethod.Wallet)) }
        )
        PaymentOptionRow(
            selected = state.selectedPaymentMethod == PaymentMethod.Card,
            title = state.selectedCard?.displayName() ?: "Kart ekle",
            subtitle = state.selectedCard?.displayExpiry() ?: "Kayıtlı kart yok",
            trailingText = state.selectedCard?.takeIf { it.isDefault }?.let { "Varsayılan" },
            onClick = {
                if (state.savedCards.isEmpty()) {
                    onIntent(TripSummaryIntent.ShowAddCardDialog)
                } else {
                    onIntent(TripSummaryIntent.SelectPaymentMethod(PaymentMethod.Card))
                }
            }
        )
        if (state.selectedPaymentMethod == PaymentMethod.Card && state.savedCards.size > 1) {
            state.savedCards.forEach { card ->
                SavedCardChoiceRow(
                    card = card,
                    selected = card.cardToken == state.selectedCard?.cardToken,
                    onClick = { onIntent(TripSummaryIntent.SelectCard(card.cardToken)) }
                )
            }
        }
        TextButton(onClick = { onIntent(TripSummaryIntent.ShowAddCardDialog) }) {
            Text("Kart ekle")
        }
    }
}

@Composable
private fun PaymentOptionRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        trailingText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A9E63)
                ),
                modifier = Modifier
                    .background(Color(0xFFE7F4EC), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SavedCardChoiceRow(
    card: SavedCard,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 26.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.cardAssociation.take(4),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = card.displayName(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = card.displayExpiry(),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        if (card.isDefault) {
            Text(
                text = "Varsayılan",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A9E63)
                )
            )
        }
    }
}

@Composable
private fun InsufficientWalletCard(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF5E6), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Bakiye yetersiz",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF8A4B00)
            )
        )
        Text(
            text = "${formatCurrency(state.walletShortfall)} eksik. Varsayılan karttan bakiye yükleyebilir veya kartla ödeyebilirsiniz.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8A4B00))
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = { onIntent(TripSummaryIntent.ShowTopUpDialog) }) {
                Text("Bakiye yükle")
            }
            TextButton(onClick = { onIntent(TripSummaryIntent.SelectPaymentMethod(PaymentMethod.Card)) }) {
                Text("Kartla öde")
            }
        }
    }
}

@Composable
private fun AddCardDialog(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isSavingCard) onIntent(TripSummaryIntent.HideAddCardDialog)
        },
        title = { Text("Kart ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ödeme ekranından çıkmadan kartınızı kaydedebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onIntent(TripSummaryIntent.SubmitCard) },
                enabled = !state.isSavingCard
            ) {
                Text(if (state.isSavingCard) "Kaydediliyor..." else "Kaydet")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(TripSummaryIntent.HideAddCardDialog) },
                enabled = !state.isSavingCard
            ) {
                Text("Vazgeç")
            }
        }
    )
}

@Composable
private fun TopUpDialog(
    state: TripSummaryState,
    onIntent: (TripSummaryIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isToppingUp) onIntent(TripSummaryIntent.HideTopUpDialog)
        },
        title = { Text("Bakiye yükle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = state.defaultCard?.let { "Varsayılan kart: ${it.displayName()}" }
                        ?: "Bakiye yüklemek için kart ekleyin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = state.topUpAmount,
                    onValueChange = { onIntent(TripSummaryIntent.UpdateTopUpAmount(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tutar") },
                    singleLine = true,
                    enabled = !state.isToppingUp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onIntent(TripSummaryIntent.SubmitTopUp) },
                enabled = !state.isToppingUp
            ) {
                Text(if (state.isToppingUp) "Yükleniyor..." else "Yükle")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(TripSummaryIntent.HideTopUpDialog) },
                enabled = !state.isToppingUp
            ) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun PaymentSuccessContent(
    result: PaymentResult,
    state: TripSummaryState,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFE7F4EC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.checkbox_on_background),
                contentDescription = null,
                tint = Color(0xFF1A9E63),
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Ödeme tamamlandı",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "${formatCurrency(result.paidAmount ?: state.totalPrice)} ${result.method?.label().orEmpty()} ile alındı.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(top = 6.dp)
        )
        result.walletBalance?.let {
            Text(
                text = "Kalan cüzdan bakiyesi ${formatCurrency(it)}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(text = "Ana sayfaya dön", onClick = onNavigateToHome)
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    isEmphasized: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = if (isEmphasized) 18.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        )
    }
}

private fun SavedCard.displayName(): String {
    return "${cardAssociation.ifBlank { "CARD" }} •••• $last4"
}

private fun SavedCard.displayExpiry(): String {
    val month = expMonth?.toString()?.padStart(2, '0')
    return if (month != null && expYear != null) {
        "Son kullanma $month/$expYear"
    } else {
        cardAlias
    }
}

private fun PaymentMethod.label(): String {
    return when (this) {
        PaymentMethod.Wallet -> "cüzdan"
        PaymentMethod.Card -> "kart"
        PaymentMethod.Iyzico -> "Iyzico"
    }
}

private fun formatCurrency(value: Double?): String {
    return "₺${String.format(Locale.US, "%.2f", value ?: 0.0)}"
}
