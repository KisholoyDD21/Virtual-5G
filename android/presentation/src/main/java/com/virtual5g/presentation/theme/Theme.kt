package com.virtual5g.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Virtual5GDarkColors = darkColorScheme(
    primary = SignalCyan,
    onPrimary = SpaceBlack,
    secondary = VirtualViolet,
    onSecondary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = HairlineStroke,
    error = StatusCritical,
    onError = TextPrimary
)

/**
 * Dark-mode-first by design (spec section 12). A light scheme is
 * intentionally not provided in this MVP - see docs/limitations.md - so
 * this theme always renders dark regardless of system setting, which is a
 * deliberate product choice for a "telecom control room" feel, not an
 * oversight.
 */
@Composable
fun Virtual5GTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Virtual5GDarkColors,
        typography = Virtual5GTypography,
        content = content
    )
}
