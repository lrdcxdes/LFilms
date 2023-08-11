package dev.lrdcxdes.lfilms.ui.theme

import android.app.Activity
import android.os.Build
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
import dev.lrdcxdes.lfilms.Theme

private val lightColorScheme = lightColorScheme(
    primary = VeryLightPink,
    secondary = VeryLightBlue,
    tertiary = Black,
    onPrimary = DarkText, // Set dark text color
    onSecondary = DarkText, // Set dark text color
    onTertiary = DarkText // Set dark text color
)

private val darkColorScheme = darkColorScheme(
    primary = DarkGray,
    secondary = LightGray,
    tertiary = LightGray,
    onPrimary = VeryLightPink, // Set light text color
    onSecondary = VeryLightBlue, // Set light text color
    onTertiary = VeryLightBlue // Set light text color
)

@Composable
fun LFilmsTheme(
    theme: Theme = Theme.LIGHT,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (theme == Theme.DARK) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }

        theme == Theme.DARK -> darkColorScheme
        else -> lightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                theme == Theme.DARK
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}