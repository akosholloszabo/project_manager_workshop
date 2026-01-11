package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val appModule = module {
    single { StorageBackend.fromPropertyValue(getProperty("storage.backend")) }
    factory<NotesStorage> {
        when (get<StorageBackend>()) {
            StorageBackend.SERVER -> ServerNotesStorage(get())
            StorageBackend.ENCRYPTED -> EncryptedNotesStorage(get(), get())
            StorageBackend.LOCAL -> PlainNotesStorage(get())
        }
    }
    factory<ProjectsStorage> {
        when (get<StorageBackend>()) {
            StorageBackend.SERVER -> ServerProjectsStorage(get())
            StorageBackend.ENCRYPTED -> EncryptedProjectsStorage(get(), get())
            StorageBackend.LOCAL -> PlainProjectsStorage(get())
        }
    }
    factory<TicketsStorage> {
        when (get<StorageBackend>()) {
            StorageBackend.SERVER -> ServerTicketsStorage(get())
            StorageBackend.ENCRYPTED -> EncryptedTicketsStorage(get(), get())
            StorageBackend.LOCAL -> PlainTicketsStorage(get())
        }
    }

    single { TicketStore(getOrNull(), get(), get()) }
    single { ProjectStore(getOrNull(), get()) }
    single { NoteStore(getOrNull(), get()) }


    single { TicketsViewModel(get(), get(), getOrNull()) }
    singleOf(::ProjectsViewModel)
    singleOf(::NotesViewModel)
}
