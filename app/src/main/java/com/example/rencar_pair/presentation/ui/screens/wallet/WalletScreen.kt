package com.example.rencar_pair.presentation.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.Blue50
import com.example.rencar_pair.ui.theme.Error50
import com.example.rencar_pair.ui.theme.Neutral90
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        WalletScreenContent(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral90)
    ) {
        TopAppBar(
            title = { Text("Cüzdanım", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            state.errorMessage?.let {
                Text(
                    text = it,
                    color = Error50,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Blue50),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mevcut Bakiye", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₺%.2f".format(state.walletInfo?.balance ?: 0.0),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onIntent(WalletIntent.ShowTopUpDialog) },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Blue50
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Bakiye Yükle",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Son İşlemler", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val transactions = state.walletInfo?.transactions ?: emptyList()
                if (transactions.isEmpty()) {
                    item {
                        Text(
                            "Henüz bir işlem bulunmuyor.",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItem(tx)
                    }
                }
            }
        }
    }

    if (state.isTopUpDialogVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(WalletIntent.HideTopUpDialog) },
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
fun TransactionItem(tx: WalletTransaction) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateStr = remember(tx.date) { formatter.format(Date(tx.date)) }
    val isTopUp = tx.type == WalletTransactionType.TOP_UP

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(if (isTopUp) "Bakiye Yükleme" else "Kiralama Ödemesi", fontWeight = FontWeight.Medium)
            Text(dateStr, color = Color.Gray, fontSize = 12.sp)
        }
        val amountStr = if (isTopUp) "+₺${tx.amount}" else "-₺${tx.amount}"
        val amountColor = if (isTopUp) Color(0xFF10B981) else Color.Black
        Text(amountStr, color = amountColor, fontWeight = FontWeight.Bold)
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
                    balance = 1500.0,
                    transactions = listOf(
                        WalletTransaction("1", 500.0, System.currentTimeMillis(), com.example.rencar_pair.domain.model.WalletTransactionType.TOP_UP),
                        WalletTransaction("2", 150.0, System.currentTimeMillis() - 86400000, com.example.rencar_pair.domain.model.WalletTransactionType.RENTAL_PAYMENT)
                    )
                )
            ),
            onIntent = {}
        )
    }
}
