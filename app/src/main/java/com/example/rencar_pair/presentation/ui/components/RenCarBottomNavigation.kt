package com.example.rencar_pair.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rencar_pair.R

enum class BottomNavRoute {
    HOME,
    HISTORY,
    WALLET,
    PROFILE
}

@Composable
fun RenCarBottomNavigation(
    currentRoute: BottomNavRoute,
    onNavigate: (BottomNavRoute) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF10151B) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF1E252E) else Color(0xFFEEF1F5)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                iconRes = R.drawable.ic_nav_map,
                label = "Harita",
                isSelected = currentRoute == BottomNavRoute.HOME,
                onClick = { if (currentRoute != BottomNavRoute.HOME) onNavigate(BottomNavRoute.HOME) }
            )
            BottomNavItem(
                iconRes = R.drawable.ic_nav_history,
                label = "Geçmiş",
                isSelected = currentRoute == BottomNavRoute.HISTORY,
                onClick = { if (currentRoute != BottomNavRoute.HISTORY) onNavigate(BottomNavRoute.HISTORY) }
            )
            BottomNavItem(
                iconRes = R.drawable.ic_nav_wallet,
                label = "Cüzdan",
                isSelected = currentRoute == BottomNavRoute.WALLET,
                onClick = { if (currentRoute != BottomNavRoute.WALLET) onNavigate(BottomNavRoute.WALLET) }
            )
            BottomNavItem(
                iconRes = R.drawable.ic_nav_profile,
                label = "Profil",
                isSelected = currentRoute == BottomNavRoute.PROFILE,
                onClick = { if (currentRoute != BottomNavRoute.PROFILE) onNavigate(BottomNavRoute.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val selectedColor = if (isDark) Color(0xFF4C95F0) else Color(0xFF0B6BCB)
    val unselectedColor = if (isDark) Color(0xFF6B7480) else Color(0xFF9AA3AE)
    
    val color = if (isSelected) selectedColor else unselectedColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = color
            )
        )
    }
}
