package com.example.rencar_pair.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rencar_pair.R
import com.example.rencar_pair.presentation.ui.components.PrimaryButton
import com.example.rencar_pair.ui.theme.PrimaryBlue
import com.example.rencar_pair.ui.theme.RenCarTheme

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingScreenContent(
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier
    )
}

@Composable
fun OnboardingScreenContent(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgGradient = if (isDark) {
        Brush.radialGradient(
            colors = listOf(Color(0xFF4C95F0).copy(alpha = 0.22f), Color.Transparent),
            radius = 400f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(PrimaryBlue.copy(alpha = 0.18f), Color.Transparent),
            radius = 400f
        )
    }
    
    val logoGradient = if (isDark) {
        Brush.linearGradient(colors = listOf(Color(0xFF3B8EF0), PrimaryBlue))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFF1E7FE0), PrimaryBlue))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(bgGradient, shape = CircleShape)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo Box
                    Box(
                        modifier = Modifier
                            .size(98.dp)
                            .shadow(
                                elevation = 44.dp,
                                shape = RoundedCornerShape(30.dp),
                                spotColor = PrimaryBlue.copy(alpha = if (isDark) 0.5f else 0.4f),
                                ambientColor = PrimaryBlue.copy(alpha = if (isDark) 0.5f else 0.4f)
                            )
                            .clip(RoundedCornerShape(30.dp))
                            .background(logoGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_car_splash),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Rencar",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 38.sp,
                            letterSpacing = (-1.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Yakındaki aracı bul,\ndakikalar içinde yola çık.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 36.dp)
            ) {
                // Pagination Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDark) Color(0xFF4C95F0) else PrimaryBlue)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDark) Color(0xFF2E3742) else Color(0xFFC7CFDA))
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDark) Color(0xFF2E3742) else Color(0xFFC7CFDA))
                    )
                }

                PrimaryButton(
                    text = "Hemen Başla",
                    onClick = onNavigateToLogin
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Zaten hesabım var · ")
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF4C95F0) else PrimaryBlue
                        )) {
                            append("Giriş yap")
                        }
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
        
        // Bottom System Navigation Indicator mock style (Optional, but in design)
        Box(
            modifier = Modifier
                .width(128.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isDark) Color(0xFFEAEEF3).copy(alpha = 0.24f) else Color(0xFF141A22).copy(alpha = 0.20f))
                .align(Alignment.BottomCenter)
                .padding(bottom = 9.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    RenCarTheme {
        OnboardingScreenContent(
            onNavigateToLogin = {}
        )
    }
}
