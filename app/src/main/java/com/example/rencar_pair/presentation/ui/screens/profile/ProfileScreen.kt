package com.example.rencar_pair.presentation.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.presentation.ui.components.BottomNavRoute
import com.example.rencar_pair.presentation.ui.components.RenCarBottomNavigation
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
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
        onNavigateToHistory = onNavigateToHistory
    )
}

@Composable
fun ProfileScreenContent(
    state: ProfileState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit
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
                        else -> {}
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = state.user?.fullName?.split(" ")
                            ?.mapNotNull { it.firstOrNull()?.toString() }
                            ?.take(2)
                            ?.joinToString("") ?: "?"
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    Text(
                        text = state.user?.fullName ?: "Misafir",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Statü: ${state.user?.role?.name ?: "Bilinmiyor"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            Text(
                                text = if (state.isFakeRepositoryMode) {
                                    "Demo modu: FAKE"
                                } else {
                                    "API modu: REAL"
                                }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Menu Items
                    ProfileMenuItem(title = "Hesabım") {}
                    ProfileMenuItem(title = "Ödeme Yöntemleri") {}
                    ProfileMenuItem(title = "Destek & Yardım") {}
                    ProfileMenuItem(title = "Hakkımızda") {}

                    Spacer(modifier = Modifier.weight(1f))

                    // Logout Button
                    Button(
                        onClick = { onIntent(ProfileIntent.Logout) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Çıkış Yap",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    fullName = "John Doe",
                    token = "token",
                    role = com.example.rencar_pair.domain.model.UserRole.Customer
                ),
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onNavigateToHome = {},
            onNavigateToHistory = {}
        )
    }
}
