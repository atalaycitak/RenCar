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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.presentation.ui.components.RenCarCameraPreview
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LicenseVerificationScreen(
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

    LicenseVerificationScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onContinue = { viewModel.onIntent(LicenseVerificationIntent.Continue) },
        onBackToLogin = onBackToLogin
    )
}

@Composable
fun LicenseVerificationScreenContent(
    state: LicenseVerificationState,
    onIntent: (LicenseVerificationIntent) -> Unit,
    onContinue: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var cameraType by remember { mutableStateOf<String?>(null) } // "FRONT" or "BACK"

    if (cameraType != null) {
        RenCarCameraPreview(
            onPhotoCaptured = { uri ->
                if (cameraType == "FRONT") {
                    onIntent(LicenseVerificationIntent.PickFrontImage(uri))
                } else {
                    onIntent(LicenseVerificationIntent.PickBackImage(uri))
                }
                cameraType = null
            },
            onCancel = { cameraType = null }
        )
        return
    }

    val hasBothImages = state.hasFrontImage && state.hasBackImage
    val phase = licenseUiPhaseFor(state.status, hasBothImages)
    val canPickImage = !state.isLoading && phase.canPickImage

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

        StatusCard(state = state, phase = phase)

        Text(
            text = helperTextFor(phase),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LicenseSideButton(
                text = "Ön yüz",
                selected = state.hasFrontImage,
                onClick = {
                    if (canPickImage) {
                        cameraType = "FRONT"
                    }
                },
                modifier = Modifier.weight(1f)
            )
            LicenseSideButton(
                text = "Arka yüz",
                selected = state.hasBackImage,
                onClick = {
                    if (canPickImage) {
                        cameraType = "BACK"
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                when (phase) {
                    LicenseUiPhase.ReadyToSubmit -> onIntent(LicenseVerificationIntent.Upload)
                    LicenseUiPhase.Reviewing,
                    LicenseUiPhase.Approved -> onContinue()
                    LicenseUiPhase.NeedsPhotos,
                    LicenseUiPhase.Rejected -> Unit
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && phase.primaryEnabled
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = primaryButtonTextFor(phase))
            }
        }
    }
}

@Composable
private fun StatusCard(
    state: LicenseVerificationState,
    phase: LicenseUiPhase
) {
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
                imageVector = if (phase == LicenseUiPhase.Approved) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Badge
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(text = statusTextFor(phase), style = MaterialTheme.typography.titleMedium)
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
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Badge,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(text = text)
        }
    }
}

private enum class LicenseUiPhase(
    val canPickImage: Boolean,
    val primaryEnabled: Boolean
) {
    NeedsPhotos(canPickImage = true, primaryEnabled = false),
    ReadyToSubmit(canPickImage = true, primaryEnabled = true),
    Reviewing(canPickImage = false, primaryEnabled = true),
    Approved(canPickImage = false, primaryEnabled = true),
    Rejected(canPickImage = true, primaryEnabled = false)
}

private fun licenseUiPhaseFor(
    status: LicenseStatus,
    hasBothImages: Boolean
): LicenseUiPhase {
    return when (status) {
        LicenseStatus.Approved -> LicenseUiPhase.Approved
        LicenseStatus.Pending -> if (hasBothImages) {
            LicenseUiPhase.Reviewing
        } else {
            LicenseUiPhase.NeedsPhotos
        }
        LicenseStatus.Rejected -> LicenseUiPhase.Rejected
        LicenseStatus.NotUploaded -> if (hasBothImages) {
            LicenseUiPhase.ReadyToSubmit
        } else {
            LicenseUiPhase.NeedsPhotos
        }
    }
}

private fun statusTextFor(phase: LicenseUiPhase): String {
    return when (phase) {
        LicenseUiPhase.NeedsPhotos -> "Ehliyet bekleniyor"
        LicenseUiPhase.ReadyToSubmit -> "Fotoğraflar hazır"
        LicenseUiPhase.Reviewing -> "Başvurunuz inceleniyor"
        LicenseUiPhase.Approved -> "Ehliyet onaylandı"
        LicenseUiPhase.Rejected -> "Ehliyet reddedildi"
    }
}

private fun helperTextFor(phase: LicenseUiPhase): String {
    return when (phase) {
        LicenseUiPhase.NeedsPhotos -> "Ön ve arka yüz fotoğrafını seçip doğrulamayı başlatın."
        LicenseUiPhase.ReadyToSubmit -> "Fotoğraflar seçildi. Başvuruyu göndermek için doğrulamayı başlatın."
        LicenseUiPhase.Reviewing -> "Başvurunuz güvenlik kontrolü için inceleniyor. Onaylandığında araç kiralama adımına devam edebilirsiniz."
        LicenseUiPhase.Approved -> "Devam ederek araç haritasına geçebilirsiniz."
        LicenseUiPhase.Rejected -> "Yeni fotoğraflar seçip tekrar doğrulama gönderebilirsiniz."
    }
}

private fun primaryButtonTextFor(phase: LicenseUiPhase): String {
    return when (phase) {
        LicenseUiPhase.NeedsPhotos,
        LicenseUiPhase.ReadyToSubmit,
        LicenseUiPhase.Rejected -> "Doğrulamayı başlat"
        LicenseUiPhase.Reviewing -> "Durumu kontrol et"
        LicenseUiPhase.Approved -> "Devam et"
    }
}

@Preview(showBackground = true)
@Composable
private fun LicenseVerificationScreenPreview() {
    RenCarTheme {
        LicenseVerificationScreenContent(
            state = LicenseVerificationState(
                status = LicenseStatus.NotUploaded,
                isLoading = false,
                errorMessage = null,
                rejectReason = null
            ),
            onIntent = {},
            onContinue = {},
            onBackToLogin = {}
        )
    }
}
