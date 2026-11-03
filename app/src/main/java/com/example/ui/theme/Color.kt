package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Theme 1: Clean Light Palette Tokens
val CleanBackground = Color(0xFFF8F9FA)      // Pure Clean Off-White Background
val CleanCard = Color(0xFFFFFFFF)            // Pure White Card Surface
val CleanCardVariant = Color(0xFFF1F5F9)     // Soft slate tinted surface
val CleanBorder = Color(0xFFE2E8F0)          // Crisp Light Grey Outline Border
val CleanBorderHover = Color(0xFFCBD5E1)     // Subtle hover / focused outline

// Primary Brand Colors (Emerald Green)
val CleanPrimary = Color(0xFF10B981)         // Forest Emerald Green
val CleanPrimaryDark = Color(0xFF059669)     // Deeper Emerald for active press
val CleanPrimaryLight = Color(0xFFECFDF5)    // Soft Mint highlight container
val CleanEmeraldLight = CleanPrimaryLight
val CleanPrimaryGlow = Color(0x2210B981)

// Typography & Content Hierarchies
val TextPrimary = Color(0xFF0F172A)          // Slate Black for High Contrast
val TextSecondary = Color(0xFF64748B)        // Neutral Slate Grey for Subtitles
val TextTertiary = Color(0xFF94A3B8)         // Muted Grey for Inactive States
val TextDisabled = Color(0xFFCBD5E1)

// Functional Category Accents (Harmonious Modern Pastels)
val CleanBlue = Color(0xFF2563EB)            // Royal Blue for Cardio / Stride
val CleanBlueLight = Color(0xFFEFF6FF)
val CleanPurple = Color(0xFF8B5CF6)          // Mind / Personality
val CleanPurpleLight = Color(0xFFF5F3FF)
val CleanAmber = Color(0xFFD97706)           // Warm Amber Streak / Warning
val CleanAmberLight = Color(0xFFFEF3C7)
val CleanCrimson = Color(0xFFEF4444)         // High-Intensity / Error
val CleanCrimsonLight = Color(0xFFFEE2E2)

// Backward-compatible semantic bindings for existing composable references
val VoidBlack = Color(0xFF0F172A)
val CyberBackground = CleanBackground
val CyberSurface = CleanCard
val CyberSurfaceVariant = CleanCardVariant
val CyberCardBorder = CleanBorder
val CyberCardElevated = CleanCard

val NeonGreen = CleanPrimary
val NeonGreenBright = CleanPrimary
val NeonGreenDark = CleanPrimaryDark
val NeonGreenMuted = CleanPrimaryLight
val NeonGreenGlow = CleanPrimaryGlow

val NeonCyan = CleanBlue
val NeonCyanDark = Color(0xFF1D4ED8)
val NeonCyanMuted = CleanBlueLight

val NeonPurple = CleanPurple
val NeonPurpleDark = Color(0xFF7C3AED)
val NeonPurpleMuted = CleanPurpleLight

val NeonAmber = CleanAmber
val NeonCrimson = CleanCrimson
