package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val mainModule = module {
    single { StorageBackend.fromPropertyValue(getProperty("storage.backend")) }

    single { NoteStore(getOrNull(), get()) }
    single { ProjectStore(getOrNull(), get()) }
    single { TicketStore(getOrNull(), get(), get()) }

    singleOf(::NotesViewModel)
    singleOf(::ProjectsViewModel)
    single { TicketsViewModel(get(), get(), getOrNull()) }
}
