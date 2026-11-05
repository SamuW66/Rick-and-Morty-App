package com.danidev.apprickmorty.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de color único basado en los tokens del Figma.
// La app es siempre "dark" (fondo casi negro), por eso no se define
// un LightColorScheme separado ni se usa dynamicColor de Material You,
// que rompería la paleta verde/cian del diseño.
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Background,
    secondary = Secondary,
    onSecondary = Background,
    tertiary = Secondary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondary,
    outline = Border
)

@Composable
fun ApprickmortyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // El diseño de Figma es siempre oscuro, así que dynamicColor
    // se deja en false para no perder la paleta verde/cian.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
