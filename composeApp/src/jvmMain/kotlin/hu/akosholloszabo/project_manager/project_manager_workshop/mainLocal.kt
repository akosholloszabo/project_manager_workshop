package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.window_title
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.FileStorageHelper
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.PlainWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.loadProperties
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel

fun main() = application {
    val backend = StorageBackend.fromPropertyValue(
        loadProperties("/koinLocal.properties").getProperty("storage.backend")
    )
    val fileStorageHelper = FileStorageHelper()
    val workingFolderStore: WorkingFolderStore = PlainWorkingFolderStore()
    val workingFolderViewModel = WorkingFolderViewModel(workingFolderStore)
    val notesStorage = PlainNotesStorage(fileStorageHelper)
    val projectsStorage = PlainProjectsStorage(fileStorageHelper)
    val ticketsStorage = PlainTicketsStorage(fileStorageHelper)
    val noteStore = NoteStore(workingFolderStore, notesStorage)
    val projectStore = ProjectStore(workingFolderStore, projectsStorage)
    val ticketStore = TicketStore(workingFolderStore, ticketsStorage, backend)
    val notesViewModel = NotesViewModel(noteStore)
    val projectsViewModel = ProjectsViewModel(projectStore)
    val ticketsViewModel = TicketsViewModel(ticketStore, projectsStorage, workingFolderStore)

    Window(
        onCloseRequest = ::exitApplication,
        title = getStringResource(Res.string.window_title),
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        App(
            storageBackend = backend,
            notesViewModel = notesViewModel,
            projectsViewModel = projectsViewModel,
            ticketsViewModel = ticketsViewModel,
            workingFolderViewModel = workingFolderViewModel,
        )
    }
}
