package com.example.rencar_pair.presentation.ui.screens.return_vehicle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.ReturnAngle
import com.example.rencar_pair.presentation.ui.components.RenCarCameraPreview
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReturnVehicleRoute(
    rentalId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: ReturnVehicleViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReturnVehicleEffect.NavigateToHome -> onNavigateToHome()
                is ReturnVehicleEffect.ShowError -> {
                    // Handled in UI state optionally
                }
            }
        }
    }

    ReturnVehicleScreen(
        rentalId = rentalId,
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun ReturnVehicleScreen(
    rentalId: String,
    state: ReturnVehicleState,
    onIntent: (ReturnVehicleIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var activeCameraAngle by remember { mutableStateOf<ReturnAngle?>(null) }

    if (activeCameraAngle != null) {
        RenCarCameraPreview(
            onPhotoCaptured = { uri ->
                onIntent(ReturnVehicleIntent.AddPhoto(angle = activeCameraAngle!!, uri = uri))
                activeCameraAngle = null
            },
            onCancel = {
                activeCameraAngle = null
            }
        )
        return
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Araç İadesi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { onIntent(ReturnVehicleIntent.SubmitReturn(rentalId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.allPhotosFilled && !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("İadeyi Tamamla", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Aracın dört köşesinden fotoğraf çekiniz.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .fillMaxHeight(0.7f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ARAÇ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AngleButton(
                    modifier = Modifier.align(Alignment.TopCenter),
                    label = "Ön",
                    isCompleted = state.frontPhotoUri != null,
                    onClick = { activeCameraAngle = ReturnAngle.FRONT }
                )

                AngleButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    label = "Arka",
                    isCompleted = state.backPhotoUri != null,
                    onClick = { activeCameraAngle = ReturnAngle.BACK }
                )

                AngleButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    label = "Sol",
                    isCompleted = state.leftPhotoUri != null,
                    onClick = { activeCameraAngle = ReturnAngle.LEFT }
                )

                AngleButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    label = "Sağ",
                    isCompleted = state.rightPhotoUri != null,
                    onClick = { activeCameraAngle = ReturnAngle.RIGHT }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AngleButton(
    modifier: Modifier = Modifier,
    label: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = 2.dp,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.Crossfade(targetState = isCompleted, label = "icon") { completed ->
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tamamlandı",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Kamera",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
