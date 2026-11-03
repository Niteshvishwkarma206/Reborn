package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RebornLightColorScheme = lightColorScheme(
  primary = CleanPrimary,              // #10B981
  onPrimary = Color.White,
  primaryContainer = CleanPrimaryLight,// #ECFDF5
  onPrimaryContainer = CleanPrimaryDark,// #059669

  secondary = CleanBlue,
  onSecondary = Color.White,
  secondaryContainer = CleanBlueLight,
  onSecondaryContainer = Color(0xFF1E40AF),

  tertiary = CleanPurple,
  onTertiary = Color.White,
  tertiaryContainer = CleanPurpleLight,
  onTertiaryContainer = Color(0xFF5B21B6),

  background = CleanBackground,        // #F8F9FA
  onBackground = TextPrimary,          // #0F172A

  surface = CleanCard,                 // #FFFFFF
  onSurface = TextPrimary,             // #0F172A
  surfaceVariant = CleanCardVariant,   // #F1F5F9
  onSurfaceVariant = TextSecondary,    // #64748B

  outline = CleanBorder,               // #E2E8F0
  outlineVariant = CleanBorderHover,

  error = CleanCrimson,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = RebornLightColorScheme,
    typography = Typography,
    content = content
  )
}
