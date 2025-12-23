package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import org.koin.dsl.module

val appModule = module {
    factory { NotesStorage }
    factory { ProjectsStorage }
    factory { TicketsStorage }

    single { WorkingFolderStore() }
    single { TicketStore(get(), get()) }
    single { ProjectStore(get(), get()) }
    single { NoteStore(get(), get()) }

    factory { WorkingFolderViewModel(get()) }
    factory {
        TicketsViewModel(
            ticketStore = get(),
            projectsStorage = get(),
            workingFolderStore = get()
        )
    }
    factory {
        ProjectsViewModel(
            projectStore = get()
        )
    }
    factory {
        NotesViewModel(
            noteStore = get()
        )
    }
}
