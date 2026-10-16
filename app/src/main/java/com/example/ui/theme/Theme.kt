package com.example.ui.theme

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
    primary = MokhtarGoldLight,
    onPrimary = MokhtarCharcoal,
    primaryContainer = MokhtarCrimsonDark,
    onPrimaryContainer = MokhtarGoldLight,
    secondary = MokhtarGold,
    onSecondary = MokhtarCharcoal,
    secondaryContainer = MokhtarDarkCard,
    onSecondaryContainer = MokhtarGoldLight,
    tertiary = MokhtarCrimsonLight,
    onTertiary = Color.White,
    background = MokhtarCharcoal,
    onBackground = MokhtarTextLightPrimary,
    surface = MokhtarDarkSurface,
    onSurface = MokhtarTextLightPrimary,
    surfaceVariant = MokhtarDarkCard,
    onSurfaceVariant = MokhtarTextLightSecondary,
    outline = Color(0xFF48454E)
)

private val LightColorScheme = lightColorScheme(
    primary = MokhtarCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = MokhtarCrimsonDark,
    secondary = MokhtarGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF2A1800),
    tertiary = MokhtarGold,
    onTertiary = MokhtarCharcoal,
    background = MokhtarLightBackground,
    onBackground = MokhtarTextPrimary,
    surface = MokhtarLightSurface,
    onSurface = MokhtarTextPrimary,
    surfaceVariant = MokhtarLightCard,
    onSurfaceVariant = MokhtarTextSecondary,
    outline = Color(0xFFD2CFD8)
)

@Composable
fun MokhtarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use Al Mokhtar signature culinary theme
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
