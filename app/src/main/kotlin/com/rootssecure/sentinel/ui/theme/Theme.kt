package com.rootssecure.sentinel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * "Digital Panopticon" dark color scheme.
 *
 * The app is dark-mode ONLY. There is intentionally no light mode — this is a
 * security command center, not a social media app. Bright screens degrade
 * situational awareness at night and break the premium aesthetic.
 */
private val SentinelDarkColorScheme = darkColorScheme(
    primary          = TealPrimary,
    onPrimary        = OnTeal,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealDim,

    secondary        = SurfaceBright,
    onSecondary      = OnBackground,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = OnSurface,

    tertiary         = CriticalRed,
    onTertiary       = OnBackground,
    tertiaryContainer = CriticalContainer,
    onTertiaryContainer = CriticalRed,

    error            = CriticalRed,
    onError          = OnBackground,
    errorContainer   = CriticalContainer,
    onErrorContainer = CriticalRed,

    background       = Background,
    onBackground     = OnBackground,

    surface          = SurfaceContainer,
    onSurface        = OnBackground,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = OnSurface,

    outline          = GlassBorder,
    outlineVariant   = GlassHighlight,
    scrim            = GlassOverlay,

    inverseSurface      = OnBackground,
    inverseOnSurface    = Background,
    inversePrimary      = TealDim,
    surfaceTint         = TealPrimary,
)

/**
 * Root composable theme wrapper. Apply at the top of [MainActivity]'s
 * `setContent` block.
 *
 * Forces dark system bars to match the obsidian [Background] color.
 */
@Composable
fun SentinelTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            // Edge-to-edge — status bar and navigation bar are transparent,
            // appearance controlled by WindowCompat
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SentinelDarkColorScheme,
        typography  = SentinelTypography,
        shapes      = SentinelShapes,
        content     = content
    )
}
