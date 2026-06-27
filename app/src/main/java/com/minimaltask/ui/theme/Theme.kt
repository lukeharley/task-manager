package com.minimaltask.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.minimaltask.data.preferences.AppThemeMode

private val LightScheme = lightColorScheme(
    primary = Ink,
    secondary = BlueSeed,
    background = Paper,
    surface = Color.White,
    surfaceVariant = Mist,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Ink,
    onSurface = Ink
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFF2F2ED),
    secondary = Color(0xFF89A7FF),
    background = Color(0xFF101112),
    surface = Color(0xFF1B1C1D),
    surfaceVariant = Color(0xFF303236)
)

@Composable
fun MinimalTaskTheme(
    mode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val scheme = when (mode) {
        AppThemeMode.LIGHT -> LightScheme
        AppThemeMode.DARK -> DarkScheme
        AppThemeMode.COLOR_BLUE -> LightScheme.withSecondary(BlueSeed)
        AppThemeMode.COLOR_GREEN -> LightScheme.withSecondary(GreenSeed)
        AppThemeMode.COLOR_ROSE -> LightScheme.withSecondary(RoseSeed)
    }
    MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
}

@Composable
fun MinimalTaskTheme(content: @Composable () -> Unit) {
    MinimalTaskTheme(if (isSystemInDarkTheme()) AppThemeMode.DARK else AppThemeMode.LIGHT, content)
}

private fun ColorScheme.withSecondary(color: Color): ColorScheme = copy(
    secondary = color,
    primary = color
)
