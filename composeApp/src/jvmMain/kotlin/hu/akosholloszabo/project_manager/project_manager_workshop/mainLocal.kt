package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.FileStorageHelper
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.PlainWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ProvideStrings
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Strings
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Texts
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import java.io.InputStream
import java.util.*

fun main() = application {
    val classLoader = Thread.currentThread().contextClassLoader
    val props = Properties().apply {
        classLoader?.getResourceAsStream("koinLocal.properties")?.use { load(it) }
        classLoader?.getResourceAsStream("strings.properties")?.use { load(it) }
    }
    val strings = Strings(props.entries.associate { (k, v) -> k.toString() to v.toString() })
    Texts.init(strings)

    // Force local storage for the local entrypoint to avoid requiring cipher/server properties.
    val storageBackend = StorageBackend.LOCAL
    val fileStorageHelper = FileStorageHelper()

    val workingFolderStore = PlainWorkingFolderStore()
    val notesStorage = PlainNotesStorage(fileStorageHelper, strings)
    val projectsStorage = PlainProjectsStorage(fileStorageHelper, strings)
    val ticketsStorage = PlainTicketsStorage(fileStorageHelper, strings)

    val noteStore = NoteStore(workingFolderStore, notesStorage)
    val projectStore = ProjectStore(workingFolderStore, projectsStorage)
    val ticketStore = TicketStore(workingFolderStore, ticketsStorage, storageBackend)

    val workingFolderViewModel = WorkingFolderViewModel(workingFolderStore)
    val notesViewModel = NotesViewModel(noteStore)
    val projectsViewModel = ProjectsViewModel(projectStore)
    val ticketsViewModel = TicketsViewModel(ticketStore, projectsStorage, workingFolderStore)

    Window(
        onCloseRequest = ::exitApplication,
        title = strings.require("window.title"),
        state = WindowState(placement = WindowPlacement.Maximized)
    ) {
        ProvideStrings(strings) {
            App(
                storageBackend = storageBackend,
                workingFolderViewModel = workingFolderViewModel,
                notesViewModel = notesViewModel,
                projectsViewModel = projectsViewModel,
                ticketsViewModel = ticketsViewModel,
            )
        }
    }
}
