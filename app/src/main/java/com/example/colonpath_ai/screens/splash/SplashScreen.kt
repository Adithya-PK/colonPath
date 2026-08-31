package com.example.colonpath_ai.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colonpath_ai.R
import com.example.colonpath_ai.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.88f) }
    val logoAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    // Infinite breathing pulse for medical halo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        // 1. Entrance animation
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )

        // 2. Content entrance
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        // 3. Smooth progress bar fill
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )

        delay(200)
        onSplashFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundLight
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Logo Container with Soft Pulse Glow
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated Soft Pulse Ring
                    Box(
                        modifier = Modifier
                            .size(142.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Blue500.copy(alpha = pulseAlpha))
                    )

                    // Logo Card
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(logoScale.value)
                            .alpha(logoAlpha.value)
                            .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = Blue500.copy(alpha = 0.18f)),
                        shape = RoundedCornerShape(26.dp),
                        color = SurfaceWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.colonpath_logo),
                                contentDescription = "ColonPath-AI Official Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Application Title
                Text(
                    text = "ColonPath-AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Navy800,
                    modifier = Modifier.alpha(contentAlpha.value)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Professional Medical Subtitle
                Text(
                    text = "AI-Assisted Colorectal Histopathology Analysis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(contentAlpha.value)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Modern Pill Progress Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.alpha(contentAlpha.value)
                ) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Blue50)
                            .border(0.5.dp, CardBorder, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressAnim.value)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Blue500, Navy800)
                                    )
                                )
                        )
                    }

                    Text(
                        text = if (progressAnim.value < 0.95f) "Initializing Neural Pipelines..." else "Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.8f)
                    )
                }
            }

            // Clinical Research Footer Tag
            Text(
                text = "Clinical Research Prototype v1.0 • Secure On-Device Architecture",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .alpha(contentAlpha.value)
            )
        }
    }
}
