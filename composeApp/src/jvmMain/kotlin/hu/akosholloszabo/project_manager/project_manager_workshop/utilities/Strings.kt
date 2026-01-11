package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Lightweight string provider replacing previous Koin-backed property lookups.
 */
class Strings(private val entries: Map<String, String>) {
    fun get(key: String): String? = entries[key]
    fun require(key: String): String = entries[key] ?: error("Missing text for key: $key")
}

/**
 * Global holder for non-Compose code paths that still need localized strings.
 */
object Texts {
    @Volatile
    private var provider: Strings? = null

    fun init(strings: Strings) {
        provider = strings
    }

    fun get(key: String): String? = provider?.get(key)

    fun require(key: String): String = provider?.require(key)
        ?: error("Texts provider not initialized for key: $key")
}

val LocalStrings = staticCompositionLocalOf<Strings> {
    error("LocalStrings is not provided")
}

@Composable
fun ProvideStrings(strings: Strings, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalStrings provides strings, content = content)
}

@Composable
fun text(key: String): String = LocalStrings.current.require(key)

