package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
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
