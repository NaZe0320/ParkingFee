package com.naze.parkingfee.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = White,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo50,
    
    secondary = Blue500,
    onSecondary = White,
    secondaryContainer = Blue600,
    onSecondaryContainer = Blue50,
    
    tertiary = Orange500,
    onTertiary = White,
    tertiaryContainer = Orange600,
    onTertiaryContainer = Orange50,
    
    error = Rose500,
    onError = White,
    errorContainer = Rose600,
    onErrorContainer = Rose50,
    
    background = Slate900,
    onBackground = Slate50,
    
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    
    outline = Slate500,
    outlineVariant = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo700,
    
    secondary = Blue600,
    onSecondary = White,
    secondaryContainer = Blue50,
    onSecondaryContainer = Blue600,
    
    tertiary = Orange600,
    onTertiary = White,
    tertiaryContainer = Orange50,
    onTertiaryContainer = Orange600,
    
    error = Rose600,
    onError = White,
    errorContainer = Rose50,
    onErrorContainer = Rose600,
    
    background = Slate50,
    onBackground = Slate900,
    
    surface = White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    
    outline = Slate400,
    outlineVariant = Slate200
)

@Composable
fun ParkingFeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 디자인 일관성을 위해 기본값 false로 변경
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}