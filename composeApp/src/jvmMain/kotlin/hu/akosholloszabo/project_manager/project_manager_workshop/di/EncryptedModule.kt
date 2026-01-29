package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageCipher
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.EncryptedWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val encryptedModule = module {
    single { StorageCipher() }
    singleOf(::EncryptedNotesStorage) bind NotesStorage::class
    singleOf(::EncryptedProjectsStorage) bind ProjectsStorage::class
    singleOf(::EncryptedTicketsStorage) bind TicketsStorage::class
    singleOf(::EncryptedWorkingFolderStore) bind WorkingFolderStore::class
}
