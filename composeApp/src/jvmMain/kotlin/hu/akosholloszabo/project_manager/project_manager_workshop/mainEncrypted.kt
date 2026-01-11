package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.FileStorageHelper
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageCipher
import hu.akosholloszabo.project_manager.project_manager_workshop.store.EncryptedWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ProvideStrings
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Strings
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Texts
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import java.util.*

fun main() = application {
    val props = Properties().apply {
        javaClass.getResourceAsStream("/koinEncrypted.properties")?.use { load(it) }
        javaClass.getResourceAsStream("/strings.properties")?.use { load(it) }
    }
    val strings = Strings(props.entries.associate { (k, v) -> k.toString() to v.toString() })
    Texts.init(strings)

    val storageBackend = StorageBackend.ENCRYPTED
    val fileStorageHelper = FileStorageHelper()
    val storageCipher = StorageCipher(strings)

    val workingFolderStore = EncryptedWorkingFolderStore(storageCipher)
    val notesStorage = EncryptedNotesStorage(storageCipher, fileStorageHelper, strings)
    val projectsStorage = EncryptedProjectsStorage(storageCipher, fileStorageHelper, strings)
    val ticketsStorage = EncryptedTicketsStorage(storageCipher, fileStorageHelper, strings)

    val noteStore = NoteStore(workingFolderStore, notesStorage)
    val projectStore = ProjectStore(workingFolderStore, projectsStorage)
    val ticketStore = TicketStore(workingFolderStore, ticketsStorage, storageBackend)

    val workingFolderViewModel = WorkingFolderViewModel(workingFolderStore)
    val notesViewModel = NotesViewModel(noteStore)
    val projectsViewModel = ProjectsViewModel(projectStore)
    val ticketsViewModel = TicketsViewModel(ticketStore, projectsStorage, workingFolderStore)

    Window(onCloseRequest = ::exitApplication, title = strings.require("window.title"), state = WindowState()) {
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
