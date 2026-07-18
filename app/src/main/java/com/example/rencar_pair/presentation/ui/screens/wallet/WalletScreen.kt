package com.example.rencar_pair.presentation.ui.screens.wallet

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.domain.model.WalletTransaction
import com.example.rencar_pair.domain.model.WalletTransactionType
import com.example.rencar_pair.presentation.ui.components.BottomNavRoute
import com.example.rencar_pair.presentation.ui.components.RenCarBottomNavigation
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun WalletScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WalletEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
                is WalletEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            RenCarBottomNavigation(
                currentRoute = BottomNavRoute.WALLET,
                onNavigate = { route ->
                    when (route) {
                        BottomNavRoute.HOME -> onNavigateToHome()
                        BottomNavRoute.HISTORY -> onNavigateToHistory()
                        BottomNavRoute.PROFILE -> onNavigateToProfile()
                        else -> {}
                    }
                }
            )
        }
    ) { padding ->
        WalletScreenContent(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun WalletScreenContent(
    state: WalletState,
    onIntent: (WalletIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.isTopUpDialogVisible) {
        TopUpDialog(state = state, onIntent = onIntent)
    }
    if (state.isAddCardDialogVisible) {
        AddCardDialog(state = state, onIntent = onIntent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Cüzdan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        item {
            BalanceCard(
                balance = state.walletInfo?.balance ?: 0.0,
                onTopUpClick = { onIntent(WalletIntent.ShowTopUpDialog) }
            )
        }

        item {
            SectionTitle(
                title = "Kayıtlı kartlar",
                actionText = "+ Ekle",
                onActionClick = { onIntent(WalletIntent.ShowAddCardDialog) }
            )
            if (state.savedCards.isEmpty()) {
                EmptySurface(text = "Kayıtlı kart yok.")
            }
        }

        items(state.savedCards, key = { it.cardToken }) { card ->
            SavedCardRow(
                card = card,
                selected = card.cardToken == state.defaultCard?.cardToken,
                onClick = { onIntent(WalletIntent.SelectCard(card.cardToken)) }
            )
        }

        item {
            SectionTitle(title = "Son işlemler")
            val transactions = state.walletInfo?.transactions.orEmpty()
            if (transactions.isEmpty()) {
                EmptySurface(text = "Henüz bir işlem bulunmuyor.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    transactions.forEachIndexed { index, transaction ->
                        TransactionItem(
                            tx = transaction,
                            isLast = index == transactions.lastIndex
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BalanceCard(balance: Double, onTopUpClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E7FE0), Color(0xFF0B6BCB))
                )
            )
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = (-28).dp, top = (-28).dp)
                .size(126.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
        )
        Column {
            Text(
                text = "RenCar bakiyesi",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.82f)
                )
            )
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(46.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onTopUpClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Bakiye yükle",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
private fun EmptySurface(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
private fun SavedCardRow(
    card: SavedCard,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 42.dp, height = 28.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.cardAssociation.take(4),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = card.displayName(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = card.displayExpiry(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        if (card.isDefault) {
            Text(
                text = "Varsayılan",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A9E63)
                ),
                modifier = Modifier
                    .background(Color(0xFFE7F4EC), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun TopUpDialog(
    state: WalletState,
    onIntent: (WalletIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isToppingUp) onIntent(WalletIntent.HideTopUpDialog)
        },
        title = { Text("Bakiye yükle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Yüklemek istediğiniz tutarı girin. İşlem tamamlanınca cüzdan bakiyesi güncellenir.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                androidx.compose.material3.OutlinedTextField(
                    value = state.topUpAmount,
                    onValueChange = { onIntent(WalletIntent.UpdateTopUpAmount(it)) },
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
                onClick = { onIntent(WalletIntent.SubmitTopUp) },
                enabled = !state.isToppingUp
            ) {
                Text(if (state.isToppingUp) "Yükleniyor..." else "Yükle")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(WalletIntent.HideTopUpDialog) },
                enabled = !state.isToppingUp
            ) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun AddCardDialog(
    state: WalletState,
    onIntent: (WalletIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isSavingCard) onIntent(WalletIntent.HideAddCardDialog)
        },
        title = { Text("Kart ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Kart numarasının tamamı sunucuya gönderilmez; API yalnızca marka, son 4 hane ve son kullanma tarihini kaydeder.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                androidx.compose.material3.OutlinedTextField(
                    value = state.cardHolderName,
                    onValueChange = { onIntent(WalletIntent.UpdateCardHolderName(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kart üzerindeki isim") },
                    singleLine = true,
                    enabled = !state.isSavingCard
                )
                androidx.compose.material3.OutlinedTextField(
                    value = state.cardNumber.chunked(4).joinToString(" "),
                    onValueChange = { onIntent(WalletIntent.UpdateCardNumber(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kart numarası") },
                    singleLine = true,
                    enabled = !state.isSavingCard,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = state.cardExpiry,
                        onValueChange = { onIntent(WalletIntent.UpdateCardExpiry(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("AA/YY") },
                        singleLine = true,
                        enabled = !state.isSavingCard,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = state.cardCvc,
                        onValueChange = { onIntent(WalletIntent.UpdateCardCvc(it)) },
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
                onClick = { onIntent(WalletIntent.SubmitCard) },
                enabled = !state.isSavingCard
            ) {
                Text(if (state.isSavingCard) "Kaydediliyor..." else "Kaydet")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(WalletIntent.HideAddCardDialog) },
                enabled = !state.isSavingCard
            ) {
                Text("Vazgeç")
            }
        }
    )
}

@Composable
fun TransactionItem(tx: WalletTransaction, isLast: Boolean) {
    val isTopUp = tx.type == WalletTransactionType.TOP_UP
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (isTopUp) Color(0xFFE7F4EC) else Color(0xFFFBEDED),
                    shape = RoundedCornerShape(11.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isTopUp) android.R.drawable.ic_input_add else android.R.drawable.ic_menu_delete
                ),
                contentDescription = null,
                tint = if (isTopUp) Color(0xFF1A9E63) else Color(0xFFE5484D),
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.description ?: if (isTopUp) "Bakiye yükleme" else "Araç kiralama",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = tx.createdAt,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Text(
            text = "${if (isTopUp) "+" else "-"}${formatCurrency(kotlin.math.abs(tx.amount))}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isTopUp) Color(0xFF1A9E63) else MaterialTheme.colorScheme.onBackground
            )
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
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

private fun formatCurrency(value: Double): String {
    return "₺${String.format(Locale.US, "%.2f", value)}"
}
