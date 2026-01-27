package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.PlainWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val plainLocalModule = module {
    singleOf(::PlainNotesStorage) bind NotesStorage::class
    singleOf(::PlainProjectsStorage) bind ProjectsStorage::class
    singleOf(::PlainTicketsStorage) bind TicketsStorage::class
    singleOf(::PlainWorkingFolderStore) bind WorkingFolderStore::class
}
