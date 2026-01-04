package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.di.appModule
import org.koin.core.context.startKoin
import org.koin.fileProperties

fun main() = application {
    startKoin {
        fileProperties()
        fileProperties("/strings.properties")
        modules(appModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Project Manager Workshop",
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        App()
    }
}
