package com.example.rencar_pair.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.presentation.ui.components.LoadingOverlay
import com.example.rencar_pair.presentation.ui.screens.auth.RegisterEffect
import com.example.rencar_pair.presentation.ui.screens.auth.RegisterIntent
import com.example.rencar_pair.presentation.ui.screens.auth.RegisterState
import com.example.rencar_pair.presentation.ui.screens.auth.RegisterViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rencar_pair.presentation.ui.components.CustomTextField
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.RenCarTheme

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToLicenseVerification: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.NavigateToLicenseVerification -> onNavigateToLicenseVerification()
            }
        }
    }

    RegisterScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier
    )
}

@Composable
fun RegisterScreenContent(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kayıt Ol",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "RenCar'a hoş geldin. Hemen kayıt ol.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(
                value = state.fullName,
                onValueChange = { onIntent(RegisterIntent.OnFullNameChanged(it)) },
                label = "Ad Soyad",
                placeholder = "Adınız Soyadınız",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = state.errorMessage != null && state.fullName.isBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = state.email,
                onValueChange = { onIntent(RegisterIntent.OnEmailChanged(it)) },
                label = "E-posta",
                placeholder = "ornek@email.com",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = state.errorMessage != null && state.email.isBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = state.phone,
                onValueChange = { onIntent(RegisterIntent.OnPhoneChanged(it)) },
                label = "Telefon",
                placeholder = "+905550000000",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                isError = state.errorMessage != null && state.phone.isBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = state.password,
                onValueChange = { onIntent(RegisterIntent.OnPasswordChanged(it)) },
                label = "Şifre",
                placeholder = "••••••••",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = state.errorMessage != null && state.password.isBlank()
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                text = "Kayıt Ol",
                onClick = { onIntent(RegisterIntent.OnRegisterClicked) },
                enabled = !state.isLoading
            )
        }

        LoadingOverlay(isLoading = state.isLoading)
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    RenCarTheme {
        RegisterScreenContent(
            state = RegisterState(),
            onIntent = {},
            onNavigateToLogin = {}
        )
    }
}
