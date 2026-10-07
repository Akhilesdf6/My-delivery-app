package com.mydelivery.manager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FDD8B),
    onPrimary = Color(0xFF003914),
    background = Color(0xFF101410),
    surface = Color(0xFF101410),
    surfaceVariant = Color(0xFF232A23),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B6B33),
    onPrimary = Color.White,
    background = Color(0xFFF7FBF5),
    surface = Color(0xFFF7FBF5),
    surfaceVariant = Color(0xFFDDE5DA),
)

/** Follows the system dark/light setting. Dark mode is fully supported. */
@Composable
fun MyDeliveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
