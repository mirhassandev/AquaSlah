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
    primary = AquaCyan,
    onPrimary = Color.Black,
    primaryContainer = DarkNavy,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = SageGreen,
    onSecondaryContainer = Color.White,
    tertiary = EmeraldGreen,
    background = GlassDarkBg,
    onBackground = Color(0xFFF0F4F8),
    surface = GlassDarkCard,
    onSurface = Color(0xFFF0F4F8),
    surfaceVariant = GlassDarkSurface,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = GlassDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = DeepOcean,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F4DE),
    onPrimaryContainer = Color(0xFF00382E),
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFF3D6),
    onSecondaryContainer = Color(0xFF4A3800),
    tertiary = SageGreen,
    background = GlassLightBg,
    onBackground = Color(0xFF0F172A),
    surface = GlassLightCard,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = GlassLightSurface,
    onSurfaceVariant = Color(0xFF475569),
    outline = GlassLightCardBorder
)

@Composable
fun AquaSlahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to maintain rich Liquid Glass brand palette
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
