package com.example.rencar_pair.presentation.ui.screens.return_vehicle

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.rencar_pair.domain.model.ReturnAngle
import com.example.rencar_pair.presentation.ui.components.RenCarCameraPreview
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReturnVehicleScreen(
    rentalId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    viewModel: ReturnVehicleViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReturnVehicleEffect.NavigateToSummary -> onNavigateToSummary(effect.rentalId)
                is ReturnVehicleEffect.ShowError -> Unit
            }
        }
    }

    ReturnVehicleScreenContent(
        rentalId = rentalId,
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun ReturnVehicleScreenContent(
    rentalId: String,
    state: ReturnVehicleState,
    onIntent: (ReturnVehicleIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var activeCameraAngle by remember { mutableStateOf<ReturnAngle?>(null) }

    activeCameraAngle?.let { angle ->
        RenCarCameraPreview(
            onPhotoCaptured = { uri ->
                onIntent(ReturnVehicleIntent.AddPhoto(angle = angle, uri = uri))
                activeCameraAngle = null
            },
            onCancel = {
                activeCameraAngle = null
            }
        )
        return
    }

    if (state.showReturnConfirmation) {
        AlertDialog(
            onDismissRequest = { onIntent(ReturnVehicleIntent.DismissReturnConfirmation) },
            title = { Text("Iadeyi onayla") },
            text = {
                Text("Dort aci fotografi kaydedildi. Arac iadesi backend'e gonderilecek.")
            },
            confirmButton = {
                TextButton(onClick = { onIntent(ReturnVehicleIntent.SubmitReturn(rentalId)) }) {
                    Text("Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(ReturnVehicleIntent.DismissReturnConfirmation) }) {
                    Text("Vazgec")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Arac Iadesi") },
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
                    onClick = { onIntent(ReturnVehicleIntent.RequestReturnConfirmation) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.allPhotosFilled && !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Iadeyi Tamamla", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aracin dort acisindan fotograf cekin, varsa hasar notunu ekleyin.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            ReturnPhotoGrid(
                state = state,
                onAngleClick = { activeCameraAngle = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.damageNote,
                onValueChange = { onIntent(ReturnVehicleIntent.UpdateDamageNote(it)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                label = { Text("Hasar notu") },
                placeholder = { Text("Hasar yoksa bos birakabilirsiniz") },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun ReturnPhotoGrid(
    state: ReturnVehicleState,
    onAngleClick: (ReturnAngle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnglePhotoCard(
                modifier = Modifier.weight(1f),
                label = "On",
                photoUri = state.frontPhotoUri,
                onClick = { onAngleClick(ReturnAngle.FRONT) }
            )
            AnglePhotoCard(
                modifier = Modifier.weight(1f),
                label = "Arka",
                photoUri = state.backPhotoUri,
                onClick = { onAngleClick(ReturnAngle.BACK) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnglePhotoCard(
                modifier = Modifier.weight(1f),
                label = "Sol",
                photoUri = state.leftPhotoUri,
                onClick = { onAngleClick(ReturnAngle.LEFT) }
            )
            AnglePhotoCard(
                modifier = Modifier.weight(1f),
                label = "Sag",
                photoUri = state.rightPhotoUri,
                onClick = { onAngleClick(ReturnAngle.RIGHT) }
            )
        }
    }
}

@Composable
private fun AnglePhotoCard(
    modifier: Modifier = Modifier,
    label: String,
    photoUri: String?,
    onClick: () -> Unit
) {
    val isCompleted = photoUri != null

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "$label fotografi",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = if (photoUri != null) 0.45f else 0f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (photoUri != null) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Crossfade(targetState = isCompleted, label = "return-photo-check") { completed ->
                    if (completed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tamamlandi",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReturnVehicleScreenPreview() {
    RenCarTheme {
        ReturnVehicleScreenContent(
            rentalId = "RNT-12345",
            state = ReturnVehicleState(
                frontPhotoUri = null,
                backPhotoUri = null,
                leftPhotoUri = null,
                rightPhotoUri = null,
                damageNote = "Sag arka tamponda hafif cizik var.",
                isLoading = false,
                errorMessage = null
            ),
            onIntent = {},
            onNavigateBack = {}
        )
    }
}
