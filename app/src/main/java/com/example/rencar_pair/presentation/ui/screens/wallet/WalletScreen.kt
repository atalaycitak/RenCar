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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.WalletTransaction
import com.example.rencar_pair.presentation.ui.components.CustomTextField
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.Blue50
import com.example.rencar_pair.ui.theme.Error50
import com.example.rencar_pair.ui.theme.Neutral90
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletState,
    onIntent: (WalletIntent) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cüzdanım", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Neutral90
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
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
                        Text("Henüz bir işlem bulunmuyor.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                    }
                } else {
                    items(transactions) { tx ->
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
    val isTopUp = tx.type.name == "TOP_UP"

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
        val amountColor = if (isTopUp) Error50 else Color.Black
        Text(amountStr, color = amountColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WalletRoute(
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WalletEffect.ShowError -> {}
                is WalletEffect.ShowMessage -> {}
            }
        }
    }

    WalletScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}
