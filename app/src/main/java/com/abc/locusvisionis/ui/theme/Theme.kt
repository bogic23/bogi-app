package com.abc.locusvisionis.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LocalDashboardColors = staticCompositionLocalOf { CyanDashboardColors }

object DashboardTheme {
    val colors: DashboardColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDashboardColors.current
}

@Composable
fun PersonalDashboardTheme(
    themeOption: ThemeOption = ThemeOption.Cyan,
    content: @Composable () -> Unit
) {
    val dashboardColors = dashboardColorsFor(themeOption)
    val colorScheme = lightColorScheme(
        primary = dashboardColors.primary,
        secondary = dashboardColors.secondary,
        tertiary = dashboardColors.accentPurple,
        background = dashboardColors.background,
        surface = dashboardColors.surface,
        surfaceVariant = dashboardColors.cardBackground,
        onPrimary = dashboardColors.surface,
        onSecondary = dashboardColors.textPrimary,
        onTertiary = dashboardColors.surface,
        onBackground = dashboardColors.textPrimary,
        onSurface = dashboardColors.textPrimary
    )
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = dashboardColors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalDashboardColors provides dashboardColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
