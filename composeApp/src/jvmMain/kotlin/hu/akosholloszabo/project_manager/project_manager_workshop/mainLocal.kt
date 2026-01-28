package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.di.localModule
import hu.akosholloszabo.project_manager.project_manager_workshop.di.mainModule
import hu.akosholloszabo.project_manager.project_manager_workshop.di.plainLocalModule
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.window_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import org.koin.core.context.startKoin
import org.koin.fileProperties

fun main() = application {
    startKoin {
        fileProperties("/koinLocal.properties")
        modules(mainModule, localModule, plainLocalModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = getStringResource(Res.string.window_title),
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        App()
    }
}
