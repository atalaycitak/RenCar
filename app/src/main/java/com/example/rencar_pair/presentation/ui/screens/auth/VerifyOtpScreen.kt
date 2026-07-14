package com.example.rencar_pair.presentation.ui.screens.auth

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
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rencar_pair.presentation.ui.components.CustomTextField
import com.example.rencar_pair.presentation.ui.components.LoadingOverlay
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpEffect
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpIntent
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpState
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpViewModel
import com.example.rencar_pair.ui.theme.RenCarTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun VerifyOtpScreen(
    onNavigateToHomeMap: () -> Unit,
    onNavigateToLicenseVerification: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerifyOtpViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VerifyOtpEffect.NavigateToHome -> onNavigateToHomeMap()
                is VerifyOtpEffect.NavigateToLicenseVerification -> onNavigateToLicenseVerification()
            }
        }
    }

    VerifyOtpScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun VerifyOtpScreenContent(
    state: VerifyOtpState,
    onIntent: (VerifyOtpIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceVariant = if (isDark) androidx.compose.ui.graphics.Color(0xFF1B212A) else androidx.compose.ui.graphics.Color(0xFFF1F4F8)
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.secondary
    val borderColor = MaterialTheme.colorScheme.outline

    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Back button icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
                    .background(surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // Standard back icon
                    contentDescription = "Geri",
                    tint = textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Doğrulama kodu",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 27.sp,
                    letterSpacing = (-0.6).sp
                ),
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append("Kod ")
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )) {
                        append(state.phone)
                    }
                    append(" numarasına SMS olarak gönderildi.")
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 23.sp
                ),
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Onay Kodu",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(9.dp))

            CustomTextField(
                value = state.code,
                onValueChange = { onIntent(VerifyOtpIntent.OnCodeChanged(it)) },
                placeholder = "123456",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                isError = state.errorMessage != null && state.code.isBlank(),
                modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(22.dp))

            PrimaryButton(
                text = "Onayla",
                onClick = { onIntent(VerifyOtpIntent.OnVerifyClicked) },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = buildAnnotatedString {
                    append("Kodu almadın mı? ")
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        append("Tekrar gönder")
                    }
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = textSecondary,
                modifier = Modifier
                    .padding(bottom = 36.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Bottom System Navigation Indicator mock style
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(5.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                    .background(if (isDark) androidx.compose.ui.graphics.Color(0xFFEAEEF3).copy(alpha = 0.24f) else androidx.compose.ui.graphics.Color(0xFF141A22).copy(alpha = 0.20f))
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 9.dp)
            )
        }

        LoadingOverlay(isLoading = state.isLoading)
    }
}

@Preview(showBackground = true)
@Composable
private fun VerifyOtpScreenPreview() {
    RenCarTheme {
        VerifyOtpScreenContent(
            state = VerifyOtpState(phone = "+905550000000"),
            onIntent = {}
        )
    }
}
