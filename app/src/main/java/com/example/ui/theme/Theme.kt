package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF386A20),
    secondary = Color(0xFF0369A1),
    tertiary = Color(0xFFB9F397),
    background = Color(0xFFFBFDF9),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFDC2626)
)

@Composable
fun LogiTrackTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
