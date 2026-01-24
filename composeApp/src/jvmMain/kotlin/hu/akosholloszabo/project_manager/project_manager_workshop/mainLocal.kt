package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.di.localModule
import hu.akosholloszabo.project_manager.project_manager_workshop.di.mainModule
import hu.akosholloszabo.project_manager.project_manager_workshop.di.plainLocalModule
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText
import org.koin.core.context.startKoin
import org.koin.fileProperties

fun main() = application {
    val koinApplication = startKoin {
        fileProperties("/koinLocal.properties")
        fileProperties("/strings.properties")
        modules(mainModule, localModule, plainLocalModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = koinApplication.koin.getText("window.title"),
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        App()
    }
}
