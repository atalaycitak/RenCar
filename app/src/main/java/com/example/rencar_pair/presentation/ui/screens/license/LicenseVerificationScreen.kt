package com.example.rencar_pair.presentation.ui.screens.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rencar_pair.domain.model.LicenseStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun LicenseVerificationRoute(
    onContinueToMap: () -> Unit,
    viewModel: LicenseVerificationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LicenseVerificationEffect.ContinueToMap -> onContinueToMap()
            }
        }
    }

    LicenseVerificationScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onContinue = viewModel::continueToMap
    )
}

@Composable
fun LicenseVerificationScreen(
    state: LicenseVerificationState,
    onIntent: (LicenseVerificationIntent) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Ehliyet dogrulama",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Arac kiralamaya baslamadan once ehliyet durumunu kontrol ediyoruz.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )

        StatusCard(state)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LicenseSideButton(
                text = "On yuz",
                selected = state.hasFrontImage,
                onClick = { onIntent(LicenseVerificationIntent.PickFrontImage) },
                modifier = Modifier.weight(1f)
            )
            LicenseSideButton(
                text = "Arka yuz",
                selected = state.hasBackImage,
                onClick = { onIntent(LicenseVerificationIntent.PickBackImage) },
                modifier = Modifier.weight(1f)
            )
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { onIntent(LicenseVerificationIntent.Upload) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = "Dogrulamayi baslat")
            }
        }

        OutlinedButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Haritaya gec")
        }
    }
}

@Composable
private fun StatusCard(state: LicenseVerificationState) {
    val statusText = when (state.status) {
        LicenseStatus.NotUploaded -> "Ehliyet bekleniyor"
        LicenseStatus.Pending -> "Inceleme bekleniyor"
        LicenseStatus.Approved -> "Ehliyet onaylandi"
        LicenseStatus.Rejected -> "Ehliyet reddedildi"
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (state.status == LicenseStatus.Approved) Icons.Default.CheckCircle else Icons.Default.Badge,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(text = statusText, style = MaterialTheme.typography.titleMedium)
                state.rejectReason?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun LicenseSideButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Badge,
                contentDescription = null
            )
            Text(text = text)
        }
    }
}
