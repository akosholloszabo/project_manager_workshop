package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {
    single { NotesStorage }
    single { ProjectsStorage }
    single { TicketsStorage }

    factory { (workingFolder: String?) -> TicketStore(workingFolder, get()) }
    factory { (workingFolder: String?) -> ProjectStore(workingFolder, get()) }
    factory { (workingFolder: String?) -> NoteStore(workingFolder, get()) }

    factory { (workingFolder: String?) ->
        TicketsViewModel(
            ticketStore = get { parametersOf(workingFolder) },
            projectsStorage = get(),
            workingFolder = workingFolder
        )
    }
    factory { (workingFolder: String?) ->
        ProjectsViewModel(
            projectStore = get { parametersOf(workingFolder) }
        )
    }
    factory { (workingFolder: String?) ->
        NotesViewModel(
            noteStore = get { parametersOf(workingFolder) }
        )
    }
}
