package com.example.rencar_pair.presentation.ui.screens.wallet

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.domain.model.WalletTransaction
import com.example.rencar_pair.domain.model.WalletTransactionType
import com.example.rencar_pair.presentation.ui.components.CustomTextField
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Cüzdan",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Balance Card
        item {
            val balance = state.walletInfo?.balance ?: 0.0
            val formattedBalance = String.format(Locale.US, "%.2f", balance)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0xFF0B6BCB).copy(alpha = 0.32f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF1E7FE0), Color(0xFF0B6BCB))
                        )
                    )
                    .padding(20.dp)
            ) {
                // Background decorative circle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = (-30).dp, top = (-30).dp)
                        .size(140.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                )
                
                Column {
                    Text(
                        text = "Rencar bakiyesi",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                    Text(
                        text = "₺$formattedBalance",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .height(46.dp)
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                            .clickable { onIntent(WalletIntent.ShowTopUpDialog) },
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
                            text = "Bakiye Yükle",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Saved Cards section
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kayıtlı kartlar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "+ Ekle",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.clickable { /* Add Card Action */ }
                )
            }

            // Mock Card 1 (VISA Default)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 28.dp)
                        .background(
                            brush = Brush.linearGradient(colors = listOf(Color(0xFF1A1F71), Color(0xFF0B6BCB))),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VISA",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, color = Color.White)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "•••• 4291",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    )
                    Text(
                        text = "Son kullanma 08/27",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Text(
                    text = "Varsayılan",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A9E63)),
                    modifier = Modifier.background(Color(0xFFE7F4EC), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mock Card 2 (MasterCard)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 28.dp)
                        .background(
                            brush = Brush.linearGradient(colors = listOf(Color(0xFFEB001B), Color(0xFFF79E1B))),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MC",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "•••• 7740",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    )
                    Text(
                        text = "Son kullanma 11/26",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Transactions
        item {
            Text(
                text = "Son işlemler",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(bottom = 11.dp)
            )
        }

        item {
            val transactions = state.walletInfo?.transactions ?: emptyList()
            if (transactions.isEmpty()) {
                Text(
                    text = "Henüz bir işlem bulunmuyor.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    transactions.forEachIndexed { index, tx ->
                        TransactionItem(tx = tx, isLast = index == transactions.size - 1)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (state.isTopUpDialogVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(WalletIntent.HideTopUpDialog) },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Bakiye Yükle") },
            text = {
                Column {
                    Text("Yüklemek istediğiniz tutarı giriniz.")
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        value = state.topUpAmount,
                        onValueChange = { onIntent(WalletIntent.UpdateTopUpAmount(it)) },
                        label = "Tutar (₺)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onIntent(WalletIntent.SubmitTopUp) },
                    enabled = !state.isToppingUp
                ) {
                    if (state.isToppingUp) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Yükle")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(WalletIntent.HideTopUpDialog) }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun TransactionItem(tx: WalletTransaction, isLast: Boolean) {
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val dateStr = remember(tx.date) { formatter.format(Date(tx.date)) }
    val isTopUp = tx.type == WalletTransactionType.TOP_UP

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon Box
        if (isTopUp) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE7F4EC), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add), // Plus
                    contentDescription = null,
                    tint = Color(0xFF1A9E63),
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFBEDED), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_delete), // Minus/Delete
                    contentDescription = null,
                    tint = Color(0xFFE5484D),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Title and Date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isTopUp) "Bakiye yükleme" else "Araç kiralama",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Amount
        val formattedAmount = String.format(Locale.US, "%.2f", tx.amount)
        val amountStr = if (isTopUp) "+₺$formattedAmount" else "−₺$formattedAmount"
        val amountColor = if (isTopUp) Color(0xFF1A9E63) else MaterialTheme.colorScheme.onBackground
        Text(
            text = amountStr,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = amountColor
            )
        )
    }
    if (!isLast) {
        androidx.compose.material3.HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WalletScreenPreview() {
    RenCarTheme {
        WalletScreenContent(
            state = WalletState(
                isLoading = false,
                walletInfo = WalletInfo(
                    balance = 340.0,
                    transactions = listOf(
                        WalletTransaction("1", 110.50, System.currentTimeMillis(), com.example.rencar_pair.domain.model.WalletTransactionType.RENTAL_PAYMENT),
                        WalletTransaction("2", 200.0, System.currentTimeMillis() - 86400000, com.example.rencar_pair.domain.model.WalletTransactionType.TOP_UP)
                    )
                )
            ),
            onIntent = {}
        )
    }
}
