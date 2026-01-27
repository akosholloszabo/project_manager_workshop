package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

import androidx.compose.runtime.Composable
import org.koin.compose.getKoin
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

object KoinUtilities {
    fun Koin.getText(key: String): String =
        getProperty(key) ?: throw Exception("title property was not set")

    fun KoinComponent.getText(key: String): String =
        getKoin().getText(key)

    // TODO that's not nice
    @Composable
    fun getText(key: String): String =
        getKoin().getText(key)
}
