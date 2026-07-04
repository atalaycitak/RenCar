package com.example.rencar_pair.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class BottomNavRoute {
    HOME,
    HISTORY,
    PROFILE
}

@Composable
fun RenCarBottomNavigation(
    currentRoute: BottomNavRoute,
    onNavigate: (BottomNavRoute) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == BottomNavRoute.HOME,
            onClick = { if (currentRoute != BottomNavRoute.HOME) onNavigate(BottomNavRoute.HOME) },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            label = { Text(text = "Harita") }
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavRoute.HISTORY,
            onClick = { if (currentRoute != BottomNavRoute.HISTORY) onNavigate(BottomNavRoute.HISTORY) },
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text(text = "Geçmiş") }
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavRoute.PROFILE,
            onClick = { if (currentRoute != BottomNavRoute.PROFILE) onNavigate(BottomNavRoute.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(text = "Profil") }
        )
    }
}
