package com.example.rencar_pair.presentation.ui.screens.license

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.LicenseStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun LicenseVerificationRoute(
    onContinueToMap: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: LicenseVerificationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        onContinue = { viewModel.onIntent(LicenseVerificationIntent.Continue) },
        onBackToLogin = onBackToLogin
    )
}

@Composable
fun LicenseVerificationScreen(
    state: LicenseVerificationState,
    onIntent: (LicenseVerificationIntent) -> Unit,
    onContinue: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val frontImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onIntent(LicenseVerificationIntent.PickFrontImage(it.toString())) }
    }
    val backImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onIntent(LicenseVerificationIntent.PickBackImage(it.toString())) }
    }
    val canUpload = !state.isLoading &&
        (state.status == LicenseStatus.NotUploaded || state.status == LicenseStatus.Rejected)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(onClick = onBackToLogin) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Giriş ekranına dön"
            )
        }

        Text(
            text = "Ehliyet doğrulama",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Araç kiralamaya başlamadan önce ehliyet durumunu kontrol ediyoruz.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )

        StatusCard(state)

        Text(
            text = helperTextFor(state.status),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LicenseSideButton(
                text = "Ön yüz",
                selected = state.hasFrontImage,
                enabled = canUpload,
                onClick = { frontImageLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            )
            LicenseSideButton(
                text = "Arka yüz",
                selected = state.hasBackImage,
                enabled = canUpload,
                onClick = { backImageLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            )
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { onIntent(LicenseVerificationIntent.Upload) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canUpload
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = uploadButtonTextFor(state.status))
            }
        }

        OutlinedButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(text = continueButtonTextFor(state.status))
        }
    }
}

@Composable
private fun StatusCard(state: LicenseVerificationState) {
    val statusText = when (state.status) {
        LicenseStatus.NotUploaded -> "Ehliyet bekleniyor"
        LicenseStatus.Pending -> "İnceleme bekleniyor"
        LicenseStatus.Approved -> "Ehliyet onaylandı"
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
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(8.dp),
        enabled = enabled
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

private fun helperTextFor(status: LicenseStatus): String {
    return when (status) {
        LicenseStatus.NotUploaded -> "Ön ve arka yüz fotoğrafını seçip doğrulamayı başlatın."
        LicenseStatus.Pending -> "Başvurunuz güvenlik kontrolü için inceleniyor. Onaylandığında araç kiralama adımına devam edebilirsiniz."
        LicenseStatus.Approved -> "Ehliyet onaylandı. Devam ederek araç haritasına geçebilirsiniz."
        LicenseStatus.Rejected -> "Ehliyet reddedildi. Yeni fotoğraflar seçip tekrar doğrulama gönderebilirsiniz."
    }
}

private fun continueButtonTextFor(status: LicenseStatus): String {
    return when (status) {
        LicenseStatus.Approved -> "Devam et"
        else -> "Durumu kontrol et"
    }
}

private fun uploadButtonTextFor(status: LicenseStatus): String {
    return when (status) {
        LicenseStatus.NotUploaded,
        LicenseStatus.Rejected -> "Doğrulamayı başlat"
        LicenseStatus.Pending -> "İnceleme bekleniyor"
        LicenseStatus.Approved -> "Ehliyet onaylandı"
    }
}
