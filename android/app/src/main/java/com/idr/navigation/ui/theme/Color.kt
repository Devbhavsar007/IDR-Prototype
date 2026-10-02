package com.idr.navigation.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * IDR Navigation Design Tokens — Google Maps-Inspired Palette
 *
 * Design philosophy: The UI should feel like Google Maps with a
 * subtle "engineering intelligence" layer underneath. Colors communicate
 * navigation health, not decoration.
 */

// ── Primary Brand ──
val IdrBlue = Color(0xFF1A73E8)         // Google Blue — primary actions
val IdrBlueDark = Color(0xFF1557B0)     // Pressed / active state
val IdrBlueLight = Color(0xFFD2E3FC)    // Light tint for cards

// ── Navigation Health Semantic Colors ──
val GnssFusionGreen = Color(0xFF0F9D58)     // GNSS+INS Fusion — healthy
val GnssFusionGreenDark = Color(0xFF0B8043)
val DegradedAmber = Color(0xFFF9AB00)       // Degraded / multipath
val DegradedAmberDark = Color(0xFFE37400)
val DeadReckoningBlue = Color(0xFF4285F4)   // Pure Dead Reckoning — active IDR
val DeadReckoningPulse = Color(0xFF1A73E8)  // DR pulse animation
val OutageCritical = Color(0xFFD93025)       // GNSS complete outage warning
val RecoveryTeal = Color(0xFF00897B)         // Recovery / re-acquisition

// ── Surface & Background ──
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceLightGray = Color(0xFFF8F9FA)
val SurfaceDark = Color(0xFF202124)
val SurfaceDarkElevated = Color(0xFF303134)
val SurfaceMapDefault = Color(0xFFF1F3F4)

// ── Text ──
val TextPrimary = Color(0xFF202124)
val TextSecondary = Color(0xFF5F6368)
val TextTertiary = Color(0xFF9AA0A6)
val TextOnDark = Color(0xFFE8EAED)
val TextOnPrimary = Color(0xFFFFFFFF)

// ── Layer Colors (Flyover / Surface / Underpass) ──
val LayerFlyover = Color(0xFF34A853)    // Elevated road — Emerald
val LayerSurface = Color(0xFF4285F4)    // At-grade — Blue
val LayerUnderpass = Color(0xFFFBBC04)  // Below-grade — Gold

// ── Misc ──
val DividerColor = Color(0xFFE8EAED)
val SheetHandle = Color(0xFFDADCE0)
val SpeedBadgeBg = Color(0xFFFFFFFF)
val SpeedBadgeBorder = Color(0xFF202124)

// ── Dark Mode Overrides ──
val DarkBackground = Color(0xFF17181A)
val DarkSurface = Color(0xFF282A2D)
val DarkCardSurface = Color(0xFF303134)
val DarkDivider = Color(0xFF3C4043)
