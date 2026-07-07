package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.components.RenCarTopBar
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeliveryChecklistScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: DeliveryChecklistViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DeliveryChecklistScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onDone = onDone
    )
}

@Composable
fun DeliveryChecklistScreenContent(
    state: DeliveryChecklistState,
    onIntent: (DeliveryChecklistIntent) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Scaffold(
        topBar = { RenCarTopBar(onBackClick = onBack, title = "Teslim kontrolü") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeliveryStatusCard(state = state)

            VehicleTelemetryCard(state = state, onIntent = onIntent)

            PhotoEvidenceCard(state = state, onIntent = onIntent)

            ChecklistRow(
                icon = Icons.Default.DirectionsCar,
                title = "Araç durumu",
                description = "Dış hasar, iç temizlik ve lastikler kontrol edildi.",
                checked = state.vehicleConditionChecked,
                onToggle = { onIntent(DeliveryChecklistIntent.ToggleVehicleCondition) }
            )
            ChecklistRow(
                icon = Icons.Default.PhotoCamera,
                title = "Fotoğraf kaydı",
                description = "En az 4 teslim fotoğrafı ve varsa hasar notu eklendi.",
                checked = state.photosChecked,
                onToggle = { onIntent(DeliveryChecklistIntent.TogglePhotos) }
            )
            ChecklistRow(
                icon = Icons.Default.Key,
                title = "Anahtar ve kapılar",
                description = "Kapılar, camlar ve anahtar teslimi kontrol edildi.",
                checked = state.doorsAndKeyChecked,
                onToggle = { onIntent(DeliveryChecklistIntent.ToggleDoorsAndKey) }
            )

            Text(
                text = "Teslim bilgileri yerel olarak doğrulanır; kiralama kaydı oluşturulduktan sonra checklist tamamlanınca aktif sürüş açılır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PrimaryButton(
                text = if (state.isCompleted) "Sürüşe başla" else "Kontrolü tamamla",
                onClick = {
                    if (state.isCompleted) {
                        onDone()
                    } else {
                        onIntent(DeliveryChecklistIntent.CompleteChecklist)
                    }
                },
                enabled = state.canComplete || state.isCompleted
            )
        }
    }
}

@Composable
private fun DeliveryStatusCard(state: DeliveryChecklistState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (state.isCompleted) "Teslim tamamlandı" else "Teslim onayı bekliyor",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = "Kiralama: ${state.rentalId}")
            Text(
                text = "Araç: ${state.vehicleId}",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun VehicleTelemetryCard(
    state: DeliveryChecklistState,
    onIntent: (DeliveryChecklistIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Araç teslim bilgileri", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.odometerKm,
                    onValueChange = { onIntent(DeliveryChecklistIntent.UpdateOdometer(it)) },
                    label = { Text("Kilometre") },
                    suffix = { Text("km") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.batteryPercent,
                    onValueChange = { onIntent(DeliveryChecklistIntent.UpdateBatteryPercent(it)) },
                    label = { Text("Şarj") },
                    suffix = { Text("%") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = state.damageNote,
                onValueChange = { onIntent(DeliveryChecklistIntent.UpdateDamageNote(it)) },
                label = { Text("Hasar notu") },
                placeholder = { Text("Hasar yoksa boş bırakılabilir") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PhotoEvidenceCard(
    state: DeliveryChecklistState,
    onIntent: (DeliveryChecklistIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Teslim fotoğrafları", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Ön, arka, sağ ve sol açı önerilir",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = "${state.photoCount}/4 minimum fotoğraf")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onIntent(DeliveryChecklistIntent.RemovePhoto) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Fotoğraf azalt")
                }
                IconButton(onClick = { onIntent(DeliveryChecklistIntent.AddPhoto) }) {
                    Icon(Icons.Default.Add, contentDescription = "Fotoğraf ekle")
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            if (checked) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                rentalId = "RNT-12345",
                vehicleId = "VHC-987",
                vehicleConditionChecked = true,
                photosChecked = false,
                doorsAndKeyChecked = false,
                odometerKm = "12450",
                batteryPercent = "82",
                photoCount = 3,
                isCompleted = false
            ),
            onIntent = {},
            onBack = {},
            onDone = {}
        )
    }
}
