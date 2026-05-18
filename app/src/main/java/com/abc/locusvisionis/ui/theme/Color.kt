package com.abc.locusvisionis.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class ThemeOption(val displayName: String) {
    Cyan("Cyan"),
    Ocean("Ocean"),
    Mint("Mint")
}

@Immutable
data class DashboardColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val accentGold: Color,
    val accentGreen: Color,
    val accentRed: Color,
    val accentOrange: Color,
    val accentPurple: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textLight: Color,
    val gradientStart: Color,
    val gradientEnd: Color
)

val CyanDashboardColors = DashboardColors(
    primary = Color(0xFF00BCD4),
    primaryVariant = Color(0xFF00ACC1),
    secondary = Color(0xFF80DEEA),
    secondaryVariant = Color(0xFF26C6DA),
    background = Color(0xFFF4FEFF),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFF8FEFF),
    accentGold = Color(0xFFFFC857),
    accentGreen = Color(0xFF14B88F),
    accentRed = Color(0xFFE85D75),
    accentOrange = Color(0xFFFF9F43),
    accentPurple = Color(0xFF7A8CFF),
    textPrimary = Color(0xFF0F2D36),
    textSecondary = Color(0xFF58737A),
    textLight = Color(0xFFA8C0C6),
    gradientStart = Color(0xFF00CFE8),
    gradientEnd = Color(0xFF7EE8FA)
)

val OceanDashboardColors = DashboardColors(
    primary = Color(0xFF0097A7),
    primaryVariant = Color(0xFF00838F),
    secondary = Color(0xFF4DD0E1),
    secondaryVariant = Color(0xFF00BCD4),
    background = Color(0xFFF3FCFD),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFF7FEFF),
    accentGold = Color(0xFFFFD166),
    accentGreen = Color(0xFF06B38A),
    accentRed = Color(0xFFD64562),
    accentOrange = Color(0xFFF79D65),
    accentPurple = Color(0xFF6C7BFF),
    textPrimary = Color(0xFF0B2830),
    textSecondary = Color(0xFF56727A),
    textLight = Color(0xFFA4BDC4),
    gradientStart = Color(0xFF0097A7),
    gradientEnd = Color(0xFF5CE1E6)
)

val MintDashboardColors = DashboardColors(
    primary = Color(0xFF1BC7C1),
    primaryVariant = Color(0xFF13B5AF),
    secondary = Color(0xFFB2F7EF),
    secondaryVariant = Color(0xFF65E5D8),
    background = Color(0xFFF5FFFE),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFF7FFFD),
    accentGold = Color(0xFFFFCB6B),
    accentGreen = Color(0xFF2ABF88),
    accentRed = Color(0xFFE66478),
    accentOrange = Color(0xFFFFA94D),
    accentPurple = Color(0xFF7E89FF),
    textPrimary = Color(0xFF12333A),
    textSecondary = Color(0xFF617C82),
    textLight = Color(0xFFB1C9CD),
    gradientStart = Color(0xFF1BC7C1),
    gradientEnd = Color(0xFFA5F3FC)
)

fun dashboardColorsFor(option: ThemeOption): DashboardColors = when (option) {
    ThemeOption.Cyan -> CyanDashboardColors
    ThemeOption.Ocean -> OceanDashboardColors
    ThemeOption.Mint -> MintDashboardColors
}
