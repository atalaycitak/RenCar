package com.example.rencar_pair.presentation.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.model.UserRole
import com.example.rencar_pair.presentation.ui.components.BottomNavRoute
import com.example.rencar_pair.presentation.ui.components.RenCarBottomNavigation
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToLogin -> onNavigateToLogin()
                is ProfileEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    ProfileScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateToHome = onNavigateToHome,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToWallet = onNavigateToWallet
    )
}

@Composable
fun ProfileScreenContent(
    state: ProfileState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            RenCarBottomNavigation(
                currentRoute = BottomNavRoute.PROFILE,
                onNavigate = { route ->
                    when (route) {
                        BottomNavRoute.HOME -> onNavigateToHome()
                        BottomNavRoute.HISTORY -> onNavigateToHistory()
                        BottomNavRoute.WALLET -> onNavigateToWallet()
                        else -> {}
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Profil",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // User Info
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = state.user?.fullName?.split(" ")
                                    ?.mapNotNull { it.firstOrNull()?.toString() }
                                    ?.take(2)
                                    ?.joinToString("") ?: "?"
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                            
                            // Names
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.user?.fullName ?: "Misafir",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                Text(
                                    text = "Telefon bilgisi yok",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            
                            // Edit Icon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.07f))
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                    contentDescription = "Profili düzenle yakında",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // License Status
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 14.dp, shape = RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFE7F4EC), RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_agenda), // Mock Document Icon
                                    contentDescription = null,
                                    tint = Color(0xFF1A9E63),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ehliyet doğrulandı",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                Text(
                                    text = "B sınıfı · geçerli",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text(
                                text = "Onaylı",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A9E63)
                                ),
                                modifier = Modifier
                                    .background(Color(0xFFE7F4EC), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Settings List
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 14.dp, shape = RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            ProfileMenuItem(
                                iconId = android.R.drawable.ic_menu_today, // Mock icon
                                title = "Ödeme yöntemleri",
                                onClick = onNavigateToWallet
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            ProfileMenuItem(
                                iconId = android.R.drawable.ic_menu_preferences, // Mock icon
                                title = "Ayarlar",
                                enabled = false,
                                onClick = {}
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            ProfileMenuItem(
                                iconId = android.R.drawable.ic_menu_help, // Mock icon
                                title = "Yardım & destek",
                                enabled = false,
                                onClick = {}
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            ProfileMenuItem(
                                iconId = android.R.drawable.ic_menu_share, // Mock icon
                                title = "Davet et · ₺50 kazan",
                                enabled = false,
                                onClick = {}
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Logout Button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 14.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                .clickable { onIntent(ProfileIntent.Logout) }
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel), // Mock Exit Icon
                                contentDescription = "Logout",
                                tint = Color(0xFFE5484D),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.size(9.dp))
                            Text(
                                text = "Çıkış yap",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE5484D)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    iconId: Int,
    title: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }
    val iconColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            )
        }
        if (enabled) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = "Yakında",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    RenCarTheme {
        ProfileScreenContent(
            state = ProfileState(
                user = User(
                    id = "1",
                    fullName = "Deniz Yılmaz",
                    token = "token",
                    role = UserRole.Customer
                ),
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onNavigateToHome = {},
            onNavigateToHistory = {},
            onNavigateToWallet = {}
        )
    }
}
