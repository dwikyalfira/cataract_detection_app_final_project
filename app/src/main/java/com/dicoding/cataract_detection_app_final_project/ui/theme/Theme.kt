package com.dicoding.cataract_detection_app_final_project.ui.theme

import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.dicoding.cataract_detection_app_final_project.data.UserPreferences

// Local composition for layout direction
val LocalLayout = staticCompositionLocalOf<LayoutDirection> { LayoutDirection.Ltr }

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlueLight,
    tertiary = PrimaryBlueDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlueLight,
    tertiary = PrimaryBlueDark,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight
)

// Theme state
sealed class ThemeMode(val value: String) {
    object Light : ThemeMode(UserPreferences.THEME_LIGHT)
    object Dark : ThemeMode(UserPreferences.THEME_DARK)
    object System : ThemeMode(UserPreferences.THEME_SYSTEM)
}

// Local composition for theme mode
val LocalThemeMode = staticCompositionLocalOf<ThemeMode> { ThemeMode.System }

@Composable
fun ProvideThemeMode(
    themeMode: ThemeMode,
    isRtl: Boolean = false,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    
    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalLayoutDirection provides layoutDirection,
    ) {
        content()
    }
}

@Composable
fun Cataract_detection_app_final_projectTheme(
    themeMode: String = UserPreferences.THEME_SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    
    // Check if the current language is RTL
    val isRtl = remember(context) {
        val config = Configuration(context.resources.configuration)
        config.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }
    
    // Layout direction is handled by the CompositionLocalProvider below
    
    val darkTheme = when (themeMode) {
        UserPreferences.THEME_LIGHT -> false
        UserPreferences.THEME_DARK -> true
        else -> systemDarkTheme // THEME_SYSTEM
    }

    val colorScheme = remember(themeMode, darkTheme) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) 
                else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    val themeModeState = remember(themeMode) {
        when (themeMode) {
            UserPreferences.THEME_LIGHT -> ThemeMode.Light
            UserPreferences.THEME_DARK -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    ProvideThemeMode(
        themeMode = themeModeState,
        isRtl = isRtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}