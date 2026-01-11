package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Simple AppTheme that uses Material3 default light/dark color schemes.
 * - `darkTheme` follows system preference by default but can be overridden.
 * - Provides LocalContentColor using the chosen color scheme so text on dark
 *   backgrounds gets the scheme's light content color by default.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(
        colorScheme = colors,
        content = {
            CompositionLocalProvider(LocalContentColor provides colors.onBackground) {
                content()
            }
        }
    )
}
