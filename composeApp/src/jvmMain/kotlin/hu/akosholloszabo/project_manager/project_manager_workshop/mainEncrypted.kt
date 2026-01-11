package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.di.appModule
import hu.akosholloszabo.project_manager.project_manager_workshop.di.localModule
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getTextOrException
import org.koin.core.context.startKoin
import org.koin.fileProperties

fun main() = application {
    val koin = startKoin {
        fileProperties("/koinEncrypted.properties")
        fileProperties("/strings.properties")
        modules(localModule, appModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = koin.koin.getTextOrException("window.title"),
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        App()
    }
}
