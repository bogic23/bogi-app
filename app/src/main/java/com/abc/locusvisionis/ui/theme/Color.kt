package com.abc.locusvisionis.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class ThemeOption(val displayName: String) {
    Cyan("Cyan"),
    Ocean("Ocean"),
    Mint("Mint")
}

enum class ThemeMode(val displayName: String) {
    Light("Light"),
    Dark("Dark")
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

val CyanDarkDashboardColors = DashboardColors(
    primary = Color(0xFF55E7F7),
    primaryVariant = Color(0xFF22D3EE),
    secondary = Color(0xFF67E8F9),
    secondaryVariant = Color(0xFF0891B2),
    background = Color(0xFF071A1F),
    surface = Color(0xFF10272D),
    cardBackground = Color(0xFF14343C),
    accentGold = Color(0xFFFFD166),
    accentGreen = Color(0xFF34D399),
    accentRed = Color(0xFFFB7185),
    accentOrange = Color(0xFFFB923C),
    accentPurple = Color(0xFFA78BFA),
    textPrimary = Color(0xFFE6FBFF),
    textSecondary = Color(0xFF9CCBD3),
    textLight = Color(0xFF6E97A0),
    gradientStart = Color(0xFF0F3C47),
    gradientEnd = Color(0xFF126B7A)
)

val OceanDarkDashboardColors = DashboardColors(
    primary = Color(0xFF3DD9EB),
    primaryVariant = Color(0xFF22C7DA),
    secondary = Color(0xFF8AE8F1),
    secondaryVariant = Color(0xFF0E7490),
    background = Color(0xFF08171C),
    surface = Color(0xFF10242B),
    cardBackground = Color(0xFF17313A),
    accentGold = Color(0xFFFFD166),
    accentGreen = Color(0xFF34D399),
    accentRed = Color(0xFFF87171),
    accentOrange = Color(0xFFFB923C),
    accentPurple = Color(0xFF93A4FF),
    textPrimary = Color(0xFFE7FBFF),
    textSecondary = Color(0xFFA1CBD3),
    textLight = Color(0xFF6F97A1),
    gradientStart = Color(0xFF103640),
    gradientEnd = Color(0xFF116072)
)

val MintDarkDashboardColors = DashboardColors(
    primary = Color(0xFF54E7DD),
    primaryVariant = Color(0xFF2DD4BF),
    secondary = Color(0xFFA7F3D0),
    secondaryVariant = Color(0xFF0F766E),
    background = Color(0xFF071917),
    surface = Color(0xFF112826),
    cardBackground = Color(0xFF183533),
    accentGold = Color(0xFFFACC15),
    accentGreen = Color(0xFF4ADE80),
    accentRed = Color(0xFFFB7185),
    accentOrange = Color(0xFFFB923C),
    accentPurple = Color(0xFF9DA8FF),
    textPrimary = Color(0xFFEBFFFC),
    textSecondary = Color(0xFFA7CDC8),
    textLight = Color(0xFF739992),
    gradientStart = Color(0xFF10413C),
    gradientEnd = Color(0xFF146B63)
)

fun dashboardColorsFor(option: ThemeOption, mode: ThemeMode): DashboardColors = when (mode) {
    ThemeMode.Light -> when (option) {
        ThemeOption.Cyan -> CyanDashboardColors
        ThemeOption.Ocean -> OceanDashboardColors
        ThemeOption.Mint -> MintDashboardColors
    }
    ThemeMode.Dark -> when (option) {
        ThemeOption.Cyan -> CyanDarkDashboardColors
        ThemeOption.Ocean -> OceanDarkDashboardColors
        ThemeOption.Mint -> MintDarkDashboardColors
    }
}
