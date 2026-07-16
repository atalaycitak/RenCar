package com.example.rencar_pair.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rencar_pair.R
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailBottomSheet(
    vehicle: Vehicle,
    onDismissRequest: () -> Unit,
    onReserveClick: () -> Unit,
    onUnlockClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fuelPercent = vehicle.fuelLevelPercent?.coerceIn(0, 100)
    val minutePrice = vehicle.pricePerMinute

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null, // Custom drag handle in content
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 30.dp, top = 14.dp)
        ) {
            // Custom drag handle
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(5.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Title & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = vehicle.title,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 21.sp,
                        letterSpacing = (-0.4).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                
                val isBusy = vehicle.status == VehicleStatus.Rented || vehicle.status == VehicleStatus.Reserved
                val statusText = when (vehicle.status) {
                    VehicleStatus.Rented -> "KULLANIMDA"
                    VehicleStatus.Reserved -> "REZERVE"
                    else -> "MÜSAİT"
                }
                val statusBgColor = if (isBusy) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE7F4EC)
                val statusTextColor = if (isBusy) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF1A9E63)
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = statusTextColor
                    ),
                    modifier = Modifier
                        .background(statusBgColor, RoundedCornerShape(7.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${vehicle.plate} · 250 m uzaklıkta", // Distance is mock
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            )

            // Vehicle Image Placeholder
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Araç fotoğrafı ekle",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Specs Row 1: Fuel & Range
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpecCardLarge(
                    modifier = Modifier.weight(1f),
                    title = "Yakıt",
                    value = fuelPercent?.let { "%$it" } ?: "Bilinmiyor",
                    iconRes = android.R.drawable.ic_menu_compass, // placeholder icon
                    bottomContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((fuelPercent ?: 0) / 100f)
                                    .fillMaxHeight()
                                    .background(Color(0xFF1FB370))
                            )
                        }
                    }
                )
                SpecCardLarge(
                    modifier = Modifier.weight(1f),
                    title = "Menzil",
                    value = "~${vehicle.rangeKm} km",
                    iconRes = android.R.drawable.ic_menu_mylocation, // placeholder icon
                    bottomContent = {
                        Text(
                            text = "Dolu depo",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                )
            }

            // Specs Row 2: Transmission & Seats
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpecCardSmall(
                    modifier = Modifier.weight(1f),
                    title = "Vites",
                    value = vehicle.transmission ?: "Bilinmiyor",
                    iconRes = android.R.drawable.ic_menu_preferences // placeholder icon
                )
                SpecCardSmall(
                    modifier = Modifier.weight(1f),
                    title = "Koltuk",
                    value = vehicle.seatCount?.let { "$it kişi" } ?: "Bilinmiyor",
                    iconRes = android.R.drawable.ic_menu_report_image // placeholder icon
                )
            }

            // Pricing Info
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Divider line manually drawn or just use a horizontalDivider
                // Actually the design has border-top on this row
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = minutePrice?.let { "₺${String.format("%.2f", it)}" }
                            ?: "₺${vehicle.pricePerDay.toInt()}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = if (minutePrice != null) " /dk" else " /gün",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Text(
                    text = "Saatlik ₺${(vehicle.pricePerHour ?: vehicle.pricePerDay).toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Button(
                    onClick = onReserveClick,
                    enabled = vehicle.canReserve,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.7.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .width(130.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Rezerve Et",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Button(
                    onClick = onUnlockClick,
                    enabled = vehicle.canUnlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kilidi Aç",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecCardLarge(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    iconRes: Int,
    bottomContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(7.dp))
            bottomContent()
        }
    }
}

@Composable
private fun SpecCardSmall(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    iconRes: Int
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}
