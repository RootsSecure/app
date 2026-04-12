package com.rootssecure.sentinel.ui.theme

import androidx.compose.ui.graphics.Color

// ── Digital Panopticon Design System ─────────────────────────────────────────
// "The app is not a dashboard. It is a command center."

// Backgrounds (obsidian layering system)
val Background        = Color(0xFF131313)  // base canvas
val SurfaceContainer  = Color(0xFF1E1E1E)  // elevated cards
val SurfaceBright     = Color(0xFF2C2C2C)  // interactive hover state
val SurfaceVariant    = Color(0xFF252525)  // input fields, inner panels

// Primary accent — neon teal "radar glow"
val TealPrimary       = Color(0xFF55D8E1)
val TealDim           = Color(0xFF00ADB5)
val TealContainer     = Color(0xFF003A3D)
val OnTeal            = Color(0xFF002022)

// Semantic alert colors
val CriticalRed       = Color(0xFFEF4444)  // CRITICAL — JCB / Escalation
val CriticalContainer = Color(0xFF3B0000)
val HighAmber         = Color(0xFFF59E0B)  // HIGH — Suspicious Activity
val HighContainer     = Color(0xFF3B2200)
val SafeGreen         = Color(0xFF10B981)  // SAFE / Healthy status
val SafeContainer     = Color(0xFF00280F)

// Text hierarchy
val OnBackground      = Color(0xFFE5E2E1)  // Primary text (off-white, not pure white)
val OnSurface         = Color(0xFFA1A1AA)  // Secondary / muted labels
val OnSurfaceVariant  = Color(0xFF71717A)  // Tertiary / hints

// Glassmorphism helpers
val GlassBorder       = Color(0x0DFFFFFF)  // 5% white — subtle depth line
val GlassHighlight    = Color(0x1AFFFFFF)  // 10% white — hover state
val GlassOverlay      = Color(0x99131313)  // 60% obsidian — modal scrim

// System bars
val AndroidStatusBar  = Background
