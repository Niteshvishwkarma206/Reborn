package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RebornDarkColorScheme = darkColorScheme(
  primary = NeonGreen,
  onPrimary = VoidBlack,
  primaryContainer = NeonGreenDark,
  onPrimaryContainer = Color.White,

  secondary = NeonGreenBright,
  onSecondary = VoidBlack,
  secondaryContainer = NeonGreenDark,
  onSecondaryContainer = Color.White,

  tertiary = NeonPurple,
  onTertiary = Color.White,
  tertiaryContainer = NeonPurpleDark,
  onTertiaryContainer = Color.White,

  background = CyberBackground,
  onBackground = TextPrimary,

  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,

  outline = CyberCardBorder,
  outlineVariant = Color(0xFF1E2638),

  error = NeonCrimson,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = RebornDarkColorScheme,
    typography = Typography,
    content = content
  )
}

