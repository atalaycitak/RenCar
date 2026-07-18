package com.example.rencar_pair.presentation.ui.screens.license

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
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
                when (cameraType) {
                    "FRONT" -> onIntent(LicenseVerificationIntent.PickFrontImage(uri))
                    "BACK" -> onIntent(LicenseVerificationIntent.PickBackImage(uri))
                    "SELFIE" -> onIntent(LicenseVerificationIntent.PickSelfieImage(uri))
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    IconButton(
                        onClick = onBackToLogin,
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(13.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column {
                        Text(
                            text = "Ehliyet doğrulama",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Kiralamadan önce tek seferlik",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                
                // Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepItem(number = 1, title = "Ehliyet", currentStep = state.currentStep)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.height(2.dp).weight(1.5f).background(if (state.currentStep >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant))
                    Spacer(modifier = Modifier.weight(1f))
                    StepItem(number = 2, title = "Selfie", currentStep = state.currentStep)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.height(2.dp).weight(1.5f).background(if (state.currentStep >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant))
                    Spacer(modifier = Modifier.weight(1f))
                    StepItem(number = 3, title = "Onay", currentStep = state.currentStep)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.currentStep == 1) {
                // Front Image Box
                Column {
                    Text(
                        text = "Ehliyet ön yüz",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (state.hasFrontImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(118.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { if (canPickImage) cameraType = "FRONT" }
                        ) {
                            Text(
                                text = "Foto",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color(0xFF1FB370), RoundedCornerShape(7.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Yüklendi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(118.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                                .clickable { if (canPickImage) cameraType = "FRONT" },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color(0xFFEAF2FC), RoundedCornerShape(13.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Ön yüzü çek veya yükle",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
    
                // Back Image Box
                Column {
                    Text(
                        text = "Ehliyet arka yüz",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (state.hasBackImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(118.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { if (canPickImage) cameraType = "BACK" }
                        ) {
                            Text(
                                text = "Foto",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color(0xFF1FB370), RoundedCornerShape(7.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Yüklendi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(118.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                                .clickable { if (canPickImage) cameraType = "BACK" },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color(0xFFEAF2FC), RoundedCornerShape(13.dp)), // Use surfaceVariant or custom light blue for dark mode compat, but keeping simple
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Arka yüzü çek veya yükle",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            } else if (state.currentStep == 2) {
                // Selfie Image Box
                Column {
                    Text(
                        text = "Selfie fotoğrafınız",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (state.hasSelfieImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { if (canPickImage) cameraType = "SELFIE" }
                        ) {
                            Text(
                                text = "Foto",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color(0xFF1FB370), RoundedCornerShape(7.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Yüklendi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                                .clickable { if (canPickImage) cameraType = "SELFIE" },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(Color(0xFFEAF2FC), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = "Yüzünüzü net gösterecek bir selfie çekin",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Info Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEAF2FC), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2C5A8C),
                    modifier = Modifier.size(18.dp).padding(top = 1.dp)
                )
                Text(
                    text = "Bilgilerin güvenle saklanır. Doğrulama genelde birkaç dakika sürer.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        color = Color(0xFF2C5A8C)
                    )
                )
            }

            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            state.rejectReason?.let {
                Text(text = "Reddedilme Nedeni: \$it", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Primary Button
            if (state.currentStep == 1) {
                PrimaryButton(
                    text = "Devam Et",
                    onClick = {
                        onIntent(LicenseVerificationIntent.NextStep)
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onIntent(LicenseVerificationIntent.PreviousStep) },
                        modifier = Modifier.weight(0.3f),
                        enabled = !state.isLoading
                    ) {
                        Text("Geri")
                    }
                    PrimaryButton(
                        text = if (state.isLoading) "İşleniyor..." else primaryButtonTextFor(phase),
                        onClick = {
                            if (phase == LicenseUiPhase.ReadyToSubmit) {
                                onIntent(LicenseVerificationIntent.Upload)
                            } else if (phase == LicenseUiPhase.Approved) {
                                onContinue()
                            }
                        },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(number: Int, title: String, currentStep: Int) {
    val isActive = number == currentStep
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
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

private fun primaryButtonTextFor(phase: LicenseUiPhase): String {
    return when (phase) {
        LicenseUiPhase.NeedsPhotos,
        LicenseUiPhase.ReadyToSubmit,
        LicenseUiPhase.Rejected -> "Doğrulamayı başlat"
        LicenseUiPhase.Reviewing -> "Durumu kontrol et"
        LicenseUiPhase.Approved -> "Devam Et"
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
                rejectReason = null,
                frontImageUri = "mock_uri",
                backImageUri = null
            ),
            onIntent = {},
            onContinue = {},
            onBackToLogin = {}
        )
    }
}
