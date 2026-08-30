package com.example.colonpath_ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColonPathColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Navy800,
    secondary = Navy600,
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Navy800,
    tertiary = GreenSuccess,
    onTertiary = Color.White,
    tertiaryContainer = GreenLight,
    onTertiaryContainer = GreenSuccess,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = DividerColor,
    error = RedError,
    onError = Color.White,
    errorContainer = RedLight,
    onErrorContainer = RedError
)

@Composable
fun ColonPathAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColonPathColorScheme,
        typography = Typography,
        content = content
    )
}