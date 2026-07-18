package com.example.rencar_pair.presentation.ui.screens.auth

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
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Info
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
import com.example.rencar_pair.R
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

    LaunchedEffect(viewModel) {
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
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceVariant = if (isDark) androidx.compose.ui.graphics.Color(0xFF1B212A) else androidx.compose.ui.graphics.Color(0xFFF1F4F8)
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.secondary
    val borderColor = MaterialTheme.colorScheme.outline
    
    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
                    .clip(RoundedCornerShape(13.dp))
                    .background(surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Tekrar hoş geldin",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 27.sp,
                    letterSpacing = (-0.6).sp
                ),
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Telefon numaranı gir, SMS ile doğrulama kodu gönderelim.",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Telefon numarası",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(9.dp))

            // Phone input row
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Prefix Box
                Box(
                    modifier = Modifier
                        .size(width = 88.dp, height = 56.dp)
                        .background(androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(15.dp))
                        .border(
                            width = 1.5.dp, 
                            color = borderColor, 
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🇹🇷 +90",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = textPrimary
                    )
                }

                // Phone Input Field
                CustomTextField(
                    value = state.phone,
                    onValueChange = { onIntent(LoginIntent.OnPhoneChanged(it)) },
                    placeholder = "532 000 00 00",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PhoneVisualTransformation(),
                    isError = state.errorMessage != null && state.phone.isBlank(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            // Info text
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Info icon
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "6 haneli kodu bu numaraya göndereceğiz. SMS ücreti operatörüne bağlıdır.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    ),
                    color = textSecondary
                )
            }

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
                text = "Kod Gönder",
                onClick = { onIntent(LoginIntent.OnLoginClicked) },
                enabled = !state.isLoading
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = buildAnnotatedString {
                    append("Hesabın yok mu? ")
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        append("Kayıt ol")
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
                    .clickable { onNavigateToRegister() }
            )
            
            // Bottom System Navigation Indicator mock style
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
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
