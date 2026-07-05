package com.example.rencar_pair.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rencar_pair.presentation.ui.components.CustomTextField
import com.example.rencar_pair.presentation.ui.components.LoadingOverlay
import com.example.rencar_pair.presentation.ui.components.PhoneVisualTransformation
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.screens.auth.LoginEffect
import com.example.rencar_pair.presentation.ui.screens.auth.LoginIntent
import com.example.rencar_pair.presentation.ui.screens.auth.LoginState
import com.example.rencar_pair.presentation.ui.screens.auth.LoginViewModel
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful Composable (Route)
 * Sadece ViewModel ile etkileşime girer ve datayı (state/intent) stateless bileşene aktarır.
 */
@Composable
fun LoginScreen(
    onNavigateToVerifyOtp: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    sessionExpired: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show session-expired message when arriving after a 401 forced logout
    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            snackbarHostState.showSnackbar("Oturumunuz sona erdi, lütfen tekrar giriş yapın.")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToVerifyOtp -> onNavigateToVerifyOtp(effect.phone)
            }
        }
    }

    // Gerçek arayüz bileşenini (stateless) çağırıyoruz
    LoginScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateToRegister = onNavigateToRegister,
        modifier = modifier
    )
}

/**
 * Stateless Composable (Screen Content)
 * ViewModel'den tamamen bağımsızdır, sadece verilen datayı (state) çizer.
 * Bu sayede kolayca @Preview yazabiliriz ve test edebiliriz.
 */
@Composable
fun LoginScreenContent(
    state: LoginState,
    snackbarHostState: SnackbarHostState,
    onIntent: (LoginIntent) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "RenCar'a Hoş Geldin",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Devam etmek için giriş yap",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            CustomTextField(
                value = state.phone,
                onValueChange = { onIntent(LoginIntent.OnPhoneChanged(it)) },
                label = "Telefon Numarası",
                placeholder = "+905550000000",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PhoneVisualTransformation(),
                isError = state.errorMessage != null && state.phone.isBlank()
            )

            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Giriş Yap",
                onClick = { onIntent(LoginIntent.OnLoginClicked) },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hesabın yok mu? Kayıt Ol",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onNavigateToRegister() }
            )
        }

        LoadingOverlay(isLoading = state.isLoading)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    RenCarTheme {
        LoginScreenContent(
            state = LoginState(
                phone = "+905550000000",
                isLoading = false,
                errorMessage = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    RenCarTheme {
        LoginScreenContent(
            state = LoginState(
                phone = "",
                isLoading = false,
                errorMessage = "Telefon numarası boş bırakılamaz"
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onNavigateToRegister = {}
        )
    }
}
