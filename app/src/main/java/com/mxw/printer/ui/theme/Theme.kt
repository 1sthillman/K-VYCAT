package com.mxw.printer.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern Professional Color Palette
val Primary = Color(0xFF6366F1) // Indigo
val PrimaryDark = Color(0xFF4F46E5)
val Secondary = Color(0xFF8B5CF6) // Purple
val Accent = Color(0xFF06B6D4) // Cyan
val AccentDark = Color(0xFF0891B2)
val Surface = Color(0xFF1E1B2E)
val SurfaceVariant = Color(0xFF2A2640)
val Background = Color(0xFF0F0D1A)
val OnPrimary = Color(0xFFFFFFFF)
val OnSurface = Color(0xFFE5E7EB)
val OnSurfaceVariant = Color(0xFF9CA3AF)
val Success = Color(0xFF10B981)
val Warning = Color(0xFFF59E0B)
val Error = Color(0xFFEF4444)
val CardSurface = Color(0xFF1F1D2E)
val CardBorder = Color(0xFF312E45)
val DividerColor = Color(0xFF2D2A3E)
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)
val TextTertiary = Color(0xFF6B7280)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = OnPrimary,
    tertiary = Accent,
    onTertiary = OnPrimary,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = CardBorder,
    error = Error,
    onError = OnPrimary
)

@Composable
fun MXWPrinterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(
            displayLarge = MaterialTheme.typography.displayLarge.copy(color = TextPrimary),
            displayMedium = MaterialTheme.typography.displayMedium.copy(color = TextPrimary),
            displaySmall = MaterialTheme.typography.displaySmall.copy(color = TextPrimary),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(color = TextPrimary),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary),
            titleLarge = MaterialTheme.typography.titleLarge.copy(color = TextPrimary),
            titleMedium = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
            titleSmall = MaterialTheme.typography.titleSmall.copy(color = TextPrimary),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(color = OnSurface),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            bodySmall = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
            labelLarge = MaterialTheme.typography.labelLarge.copy(color = OnSurface),
            labelMedium = MaterialTheme.typography.labelMedium.copy(color = TextSecondary),
            labelSmall = MaterialTheme.typography.labelSmall.copy(color = TextTertiary),
        ),
        content = content
    )
}
