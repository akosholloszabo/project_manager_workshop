package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.FileStorageHelper
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageCipher
import hu.akosholloszabo.project_manager.project_manager_workshop.store.EncryptedWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.PlainWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val localModule = module {
    factoryOf(::FileStorageHelper)
    factoryOf(::StorageCipher)
    singleOf(::WorkingFolderViewModel)
    single<WorkingFolderStore> {
        when (get<StorageBackend>()) {
            StorageBackend.ENCRYPTED -> EncryptedWorkingFolderStore(get())
            else -> PlainWorkingFolderStore()
        }
    }
}
