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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.components.RenCarTopBar
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeliveryChecklistRoute(
    rentalId: String,
    vehicleId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: DeliveryChecklistViewModel = koinViewModel(
        parameters = { parametersOf(rentalId, vehicleId) }
    )
) {
    val state by viewModel.state.collectAsState()
    DeliveryChecklistScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onDone = onDone
    )
}

@Composable
fun DeliveryChecklistScreen(
    state: DeliveryChecklistState,
    onIntent: (DeliveryChecklistIntent) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Scaffold(
        topBar = { RenCarTopBar(onBackClick = onBack, title = "Teslim checklist") }
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
                        text = if (state.isCompleted) "Teslim tamamlandi" else "Teslim onayi bekliyor",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(text = "Kiralama: ${state.rentalId}")
                }
            }

            ChecklistRow(
                icon = Icons.Default.DirectionsCar,
                title = "Arac durumu",
                description = "Dis hasar, yakit/sarj ve temizlik kontrol edildi.",
                checked = state.vehicleConditionChecked,
                onToggle = { onIntent(DeliveryChecklistIntent.ToggleVehicleCondition) }
            )
            ChecklistRow(
                icon = Icons.Default.PhotoCamera,
                title = "Fotograf kaydi",
                description = "Arac teslim fotograflari kullanici tarafinda onaylandi.",
                checked = state.photosChecked,
                onToggle = { onIntent(DeliveryChecklistIntent.TogglePhotos) }
            )
            ChecklistRow(
                icon = Icons.Default.Key,
                title = "Anahtar ve kapilar",
                description = "Kapilar ve anahtar teslimi kontrol edildi.",
                checked = state.doorsAndKeyChecked,
                onToggle = { onIntent(DeliveryChecklistIntent.ToggleDoorsAndKey) }
            )

            Text(
                text = "Not: Teslim checklist adimlari su an local state ile tutulur; backend bu adimlar icin ayri endpoint sunmuyor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PrimaryButton(
                text = if (state.isCompleted) "Haritaya don" else "Checklist'i tamamla",
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
