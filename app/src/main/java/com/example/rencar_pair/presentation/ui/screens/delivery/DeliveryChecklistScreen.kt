package com.example.rencar_pair.presentation.ui.screens.delivery

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rencar_pair.R
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeliveryChecklistScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: DeliveryChecklistViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingSide by remember { mutableStateOf<RentalPhotoSide?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val side = pendingSide
        pendingSide = null
        if (side != null && uri != null) {
            viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(side, uri.toString()))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DeliveryChecklistEffect.ChecklistCompleted -> onDone()
                is DeliveryChecklistEffect.ShowError -> Unit
            }
        }
    }

    DeliveryChecklistScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onDone = onDone,
        onPickPhoto = { side ->
            pendingSide = side
            photoPicker.launch("image/*")
        }
    )
}

@Composable
fun DeliveryChecklistScreenContent(
    state: DeliveryChecklistState,
    onIntent: (DeliveryChecklistIntent) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onPickPhoto: (RentalPhotoSide) -> Unit
) {
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
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(13.dp))
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column {
                        Text(
                            text = "Araç durumu",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Başlamadan önce 4 yönü çek",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle info and counter
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Renault Clio · 34 RNC 022", // Mock
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "${state.completedPhotoCount} / 4 çekildi",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PhotoBox(
                    modifier = Modifier.weight(1f),
                    title = "Ön",
                    isTaken = state.frontPhotoTaken,
                    isUploading = state.uploadingSide == RentalPhotoSide.Front,
                    onClick = { onPickPhoto(RentalPhotoSide.Front) }
                )
                PhotoBox(
                    modifier = Modifier.weight(1f),
                    title = "Arka",
                    isTaken = state.backPhotoTaken,
                    isUploading = state.uploadingSide == RentalPhotoSide.Back,
                    onClick = { onPickPhoto(RentalPhotoSide.Back) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PhotoBox(
                    modifier = Modifier.weight(1f),
                    title = "Sol",
                    isTaken = state.leftPhotoTaken,
                    isUploading = state.uploadingSide == RentalPhotoSide.Left,
                    onClick = { onPickPhoto(RentalPhotoSide.Left) }
                )
                PhotoBox(
                    modifier = Modifier.weight(1f),
                    title = "Sağ",
                    isTaken = state.rightPhotoTaken,
                    isUploading = state.uploadingSide == RentalPhotoSide.Right,
                    onClick = { onPickPhoto(RentalPhotoSide.Right) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Warning message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_alert), // Yellow alert icon
                    contentDescription = null,
                    tint = Color(0xFFE6A700),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Hasarları net çek — teslim sonrası anlaşmazlığı önler.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Bottom Action
            if (state.isCompleted) {
                PrimaryButton(
                    text = "Haritaya Dön",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    text = when {
                        state.isUploading -> "Yükleniyor..."
                        state.canComplete -> "Kiralamayı Başlat"
                        else -> "Kiralamayı Başlat · ${4 - state.completedPhotoCount} foto kaldı"
                    },
                    onClick = { onIntent(DeliveryChecklistIntent.CompleteChecklist) },
                    enabled = state.canComplete && !state.isUploading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PhotoBox(
    modifier: Modifier = Modifier,
    title: String,
    isTaken: Boolean,
    isUploading: Boolean = false,
    onClick: () -> Unit
) {
    if (isUploading) {
        Box(
            modifier = modifier
                .height(158.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }
        return
    }

    if (isTaken) {
        Box(
            modifier = modifier
                .height(158.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(7.dp)).padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                )
            }
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp).background(Color(0xFF1FB370), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_edit), // placeholder check
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .height(158.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp)).padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_camera),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Galeriden seç",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeliveryChecklistScreenPreview() {
    RenCarTheme {
        DeliveryChecklistScreenContent(
            state = DeliveryChecklistState(
                rentalId = "RNT-123",
                vehicleId = "VHC-123",
                frontPhotoTaken = true,
                backPhotoTaken = true,
                leftPhotoTaken = false,
                rightPhotoTaken = false
            ),
            onIntent = {},
            onBack = {},
            onDone = {},
            onPickPhoto = {}
        )
    }
}
