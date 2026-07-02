package com.example.rencar_pair.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.presentation.ui.components.CustomTextField
import com.example.rencar_pair.presentation.ui.components.LoadingOverlay
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpEffect
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpIntent
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun VerifyOtpScreen(
    onNavigateToHomeMap: () -> Unit,
    onNavigateToLicenseVerification: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerifyOtpViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VerifyOtpEffect.NavigateToHome -> onNavigateToHomeMap()
                is VerifyOtpEffect.NavigateToLicenseVerification -> onNavigateToLicenseVerification()
                is VerifyOtpEffect.ShowError -> Unit
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Message,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Doğrulama Kodu",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${state.phone} numarasına gönderilen kodu girin (Simülasyon: 123456)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            CustomTextField(
                value = state.code,
                onValueChange = { viewModel.onIntent(VerifyOtpIntent.OnCodeChanged(it)) },
                label = "OTP Kodu",
                placeholder = "123456",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                isError = state.errorMessage != null && state.code.isBlank()
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
                text = "Doğrula",
                onClick = { viewModel.onIntent(VerifyOtpIntent.OnVerifyClicked) },
                enabled = !state.isLoading
            )
        }

        LoadingOverlay(isLoading = state.isLoading)
    }
}
