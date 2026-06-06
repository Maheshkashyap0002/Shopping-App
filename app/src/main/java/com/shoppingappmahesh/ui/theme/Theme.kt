package com.shoppingappmahesh.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color.Blue,
    secondary = PlantGreen,
    tertiary = PlantLightGreen,
    background = PlantDarkGreen,
    surface = DarkGray,
    onPrimary = Color.White,
    onSecondary = SoftWhite,
    onTertiary = PlantDarkGreen,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Blue,
    secondary = PlantAccentGreen,
    tertiary = PlantLightGreen,
    background = SoftWhite,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = PlantDarkGreen,
    onTertiary = PlantDarkGreen,
    onBackground = DarkGray,
    onSurface = DarkGray,
)

@Composable
fun ShoppingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}