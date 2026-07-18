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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
        contract = ActivityResultContracts.OpenDocument()
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
            photoPicker.launch(arrayOf("image/jpeg", "image/png"))
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    photoUri = state.selectedPhotoUri(RentalPhotoSide.Front),
                    hasError = state.hasUploadFailed(RentalPhotoSide.Front),
                    isUploading = state.uploadingSide == RentalPhotoSide.Front,
                    onClick = { onPickPhoto(RentalPhotoSide.Front) }
                )
                PhotoBox(
                    modifier = Modifier.weight(1f),
                    title = "Arka",
                    isTaken = state.backPhotoTaken,
                    photoUri = state.selectedPhotoUri(RentalPhotoSide.Back),
                    hasError = state.hasUploadFailed(RentalPhotoSide.Back),
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
                    photoUri = state.selectedPhotoUri(RentalPhotoSide.Left),
                    hasError = state.hasUploadFailed(RentalPhotoSide.Left),
                    isUploading = state.uploadingSide == RentalPhotoSide.Left,
                    onClick = { onPickPhoto(RentalPhotoSide.Left) }
                )
                PhotoBox(
                    modifier = Modifier.weight(1f),
                    title = "Sağ",
                    isTaken = state.rightPhotoTaken,
                    photoUri = state.selectedPhotoUri(RentalPhotoSide.Right),
                    hasError = state.hasUploadFailed(RentalPhotoSide.Right),
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
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFE6A700),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Hasarları net çek. Teslim sonrası anlaşmazlığı önler.",
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
                        else -> "Kiralamayı Başlat - ${state.remainingPhotoCount} foto kaldı"
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
    photoUri: String?,
    hasError: Boolean,
    isUploading: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isTaken -> Color(0xFF1FB370)
        isUploading -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val showPreview = photoUri != null

    Box(
        modifier = modifier
            .height(158.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor.copy(alpha = if (isTaken || hasError) 0.9f else 0.55f), RoundedCornerShape(18.dp))
            .clickable(enabled = !isUploading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (showPreview) {
            AsyncImage(
                model = photoUri,
                contentDescription = "$title fotoğrafı",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (hasError) 0.48f else 0.28f))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(
                    if (showPreview) Color.Black.copy(alpha = 0.52f) else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(7.dp)
                )
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showPreview) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        when {
            isUploading -> PhotoStatusContent(
                text = "Yükleniyor",
                icon = null,
                textColor = if (showPreview) Color.White else MaterialTheme.colorScheme.primary
            )
            hasError -> PhotoStatusContent(
                text = "Tekrar seç",
                icon = Icons.Default.Refresh,
                textColor = Color.White
            )
            isTaken -> UploadedBadge()
            else -> PhotoStatusContent(
                text = "Galeriden seç",
                icon = Icons.Default.CameraAlt,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoStatusContent(
    text: String,
    icon: ImageVector?,
    textColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (icon == null) Color.Transparent else MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (icon == null) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp), color = MaterialTheme.colorScheme.primary)
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}

@Composable
private fun UploadedBadge() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFF1FB370), RoundedCornerShape(13.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "Yüklendi",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
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
